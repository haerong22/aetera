package io.aetera.usecase.auth

import io.aetera.model.auth.AccessTokenProvider
import io.aetera.model.auth.AuthCredential
import io.aetera.model.auth.AuthCredentialId
import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.AuthErrorCode
import io.aetera.model.auth.AuthProvider
import io.aetera.model.auth.EncryptedPassword
import io.aetera.model.auth.IssuedAccessToken
import io.aetera.model.auth.PasswordEncryptor
import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.user.Email
import io.aetera.model.user.User
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.model.user.UserStatus
import io.aetera.shared.error.CoreException
import io.aetera.usecase.auth.cmd.LoginCommand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class LoginServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)
        val userRepository = mockk<UserRepository>()
        val authCredentialRepository = mockk<AuthCredentialRepository>()
        val passwordEncryptor = mockk<PasswordEncryptor>()
        val accessTokenProvider = mockk<AccessTokenProvider>()
        val refreshTokenRepository = mockk<RefreshTokenRepository>()
        val sessionIssuer = SessionIssuer(accessTokenProvider, refreshTokenRepository, clock)
        val sut = LoginService(userRepository, authCredentialRepository, passwordEncryptor, sessionIssuer)

        val hash = EncryptedPassword("encrypted")

        fun user(status: UserStatus = UserStatus.ACTIVE) = User.reconstitute(
            id = UserId.next(),
            email = Email("hong@example.com"),
            nickname = "홍길동",
            timezone = User.DEFAULT_TIMEZONE,
            status = status,
            registeredAt = now.minusSeconds(86_400),
            withdrawnAt = null,
        )

        fun credential(userId: UserId) = AuthCredential.email(AuthCredentialId.next(), userId, hash, now)

        val command = LoginCommand("hong@example.com", "password1234")

        beforeTest {
            clearMocks(userRepository, authCredentialRepository, passwordEncryptor, accessTokenProvider, refreshTokenRepository)
            every { refreshTokenRepository.save(any()) } answers { firstArg() }
            every { accessTokenProvider.issue(any()) } returns IssuedAccessToken("access-token", 900)
        }

        it("비밀번호가 맞으면 액세스 토큰과 리프레시 토큰 한 쌍을 발급한다") {
            val hong = user()
            every { userRepository.getByEmail(Email("hong@example.com")) } returns hong
            every { authCredentialRepository.getByUserIdAndProvider(hong.id, AuthProvider.EMAIL) } returns credential(hong.id)
            every { passwordEncryptor.matches("password1234", hash) } returns true

            val result = sut.login(command)

            result.accessToken shouldBe "access-token"
            result.user.email shouldBe "hong@example.com"
            verify(exactly = 1) { refreshTokenRepository.save(any()) }
        }

        it("없는 이메일이든 틀린 비밀번호든 같은 코드로 거절한다 — 가입 여부를 노출하지 않는다") {
            every { userRepository.getByEmail(Email("hong@example.com")) } returns null

            shouldThrow<CoreException> { sut.login(command) }.errorCode shouldBe AuthErrorCode.LOGIN_FAILED

            val hong = user()
            every { userRepository.getByEmail(Email("hong@example.com")) } returns hong
            every { authCredentialRepository.getByUserIdAndProvider(hong.id, AuthProvider.EMAIL) } returns credential(hong.id)
            every { passwordEncryptor.matches("password1234", hash) } returns false

            shouldThrow<CoreException> { sut.login(command) }.errorCode shouldBe AuthErrorCode.LOGIN_FAILED
        }

        it("탈퇴한 사용자는 로그인할 수 없다") {
            every { userRepository.getByEmail(Email("hong@example.com")) } returns user(UserStatus.WITHDRAWN)

            shouldThrow<CoreException> { sut.login(command) }.errorCode shouldBe AuthErrorCode.LOGIN_FAILED
            verify(exactly = 0) { refreshTokenRepository.save(any()) }
        }
    })
