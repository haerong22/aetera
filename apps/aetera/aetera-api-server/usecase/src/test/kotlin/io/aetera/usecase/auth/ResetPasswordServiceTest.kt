package io.aetera.usecase.auth

import io.aetera.model.auth.AuthCredential
import io.aetera.model.auth.AuthCredentialId
import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.AuthErrorCode
import io.aetera.model.auth.AuthProvider
import io.aetera.model.auth.EncryptedPassword
import io.aetera.model.auth.OpaqueToken
import io.aetera.model.auth.PasswordEncryptor
import io.aetera.model.auth.PasswordResetToken
import io.aetera.model.auth.PasswordResetTokenId
import io.aetera.model.auth.PasswordResetTokenRepository
import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.user.Email
import io.aetera.model.user.User
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.model.user.UserStatus
import io.aetera.shared.error.CoreException
import io.aetera.usecase.auth.cmd.ResetPasswordCommand
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

/**
 * 이 토큰 하나가 지금 비밀번호를 묻지 않고 계정을 넘긴다. 그래서 무엇을 **거절하는가**가
 * 기능의 절반이고, 거절 이유를 **구분해서 알려 주지 않는 것**이 나머지 절반이다.
 */
class ResetPasswordServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-09-29T12:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)

        val userRepository = mockk<UserRepository>()
        val authCredentialRepository = mockk<AuthCredentialRepository>(relaxed = true)
        val tokenRepository = mockk<PasswordResetTokenRepository>(relaxed = true)
        val refreshTokenRepository = mockk<RefreshTokenRepository>(relaxed = true)
        val passwordEncryptor = mockk<PasswordEncryptor>()

        val sut =
            ResetPasswordService(
                userRepository,
                authCredentialRepository,
                tokenRepository,
                refreshTokenRepository,
                passwordEncryptor,
                clock,
            )

        val owner = UserId.next()
        val raw = "raw-token"
        val newHash = EncryptedPassword("새-해시")

        fun user(status: UserStatus = UserStatus.ACTIVE) = User.reconstitute(
            id = owner,
            email = Email("hong@example.com"),
            nickname = "홍길동",
            timezone = User.DEFAULT_TIMEZONE,
            status = status,
            registeredAt = now.minusSeconds(86_400),
        )

        fun token(
            issuedAt: Instant = now.minusSeconds(60),
            usedAt: Instant? = null,
        ) = PasswordResetToken.reconstitute(
            id = PasswordResetTokenId.next(),
            userId = owner,
            tokenHash = OpaqueToken.hash(raw),
            issuedAt = issuedAt,
            expiresAt = issuedAt.plusSeconds(1800),
            usedAt = usedAt,
        )

        val command = ResetPasswordCommand(raw, "newpassword5678")

        beforeTest {
            clearMocks(userRepository, authCredentialRepository, tokenRepository, refreshTokenRepository, passwordEncryptor)
            every { tokenRepository.getByTokenHash(OpaqueToken.hash(raw)) } returns token()
            every { userRepository.getById(owner) } returns user()
            every { authCredentialRepository.getByUserIdAndProvider(owner, AuthProvider.EMAIL) } returns
                AuthCredential.email(AuthCredentialId.next(), owner, EncryptedPassword("옛-해시"), now)
            every { authCredentialRepository.save(any()) } answers { firstArg() }
            every { passwordEncryptor.encrypt("newpassword5678") } returns newHash
        }

        describe("바꾸기") {
            it("새 해시를 저장한다") {
                sut.reset(command)

                verify { authCredentialRepository.save(match { it.passwordHash == newHash }) }
            }

            /*
             * 여러 번 요청해 링크가 여러 통 나가 있을 수 있다. 하나를 쓰고 나머지가 살아 있으면
             * 받은 편지함에 남은 다른 링크로 방금 정한 비밀번호를 또 바꿀 수 있다.
             */
            it("쓴 토큰과 남은 토큰을 함께 죽인다") {
                sut.reset(command)

                verify { tokenRepository.markAllUsedByUserId(owner, now) }
            }

            /*
             * 비밀번호를 잊은 것이 아니라 빼앗긴 것일 수 있다. 세션을 남겨 두면
             * 비밀번호만 바뀌고 침입자는 안에 그대로 있다.
             */
            it("모든 세션을 끊는다") {
                sut.reset(command)

                verify { refreshTokenRepository.revokeAllByUserId(owner, now) }
            }

            // 여기까지 온 사람이 주인인지 아는 근거는 메일함을 열었다는 것뿐이다. 로그인은 새로 한다.
            it("세션을 새로 주지 않는다") {
                sut.reset(command)

                verify(exactly = 0) { refreshTokenRepository.save(any()) }
            }
        }

        describe("거절") {
            /*
             * 없는 토큰·만료된 토큰·이미 쓴 토큰이 **모두 같은 코드**여야 한다. 나누면
             * 링크를 주워 온 사람에게 그 링크가 진짜였다는 사실을 알려 주는 셈이다.
             */
            it("없는 토큰·만료된 토큰·쓴 토큰이 모두 같은 코드다") {
                every { tokenRepository.getByTokenHash(any()) } returns null
                val missing = shouldThrow<CoreException> { sut.reset(command) }.errorCode

                every { tokenRepository.getByTokenHash(any()) } returns token(issuedAt = now.minusSeconds(3600))
                val expired = shouldThrow<CoreException> { sut.reset(command) }.errorCode

                every { tokenRepository.getByTokenHash(any()) } returns token(usedAt = now.minusSeconds(10))
                val used = shouldThrow<CoreException> { sut.reset(command) }.errorCode

                missing shouldBe AuthErrorCode.INVALID_PASSWORD_RESET_TOKEN
                expired shouldBe AuthErrorCode.INVALID_PASSWORD_RESET_TOKEN
                used shouldBe AuthErrorCode.INVALID_PASSWORD_RESET_TOKEN
            }

            it("거절하면 아무것도 바꾸지 않는다") {
                every { tokenRepository.getByTokenHash(any()) } returns token(usedAt = now)

                shouldThrow<CoreException> { sut.reset(command) }

                verify(exactly = 0) { authCredentialRepository.save(any()) }
                verify(exactly = 0) { refreshTokenRepository.revokeAllByUserId(any(), any()) }
                verify(exactly = 0) { tokenRepository.markAllUsedByUserId(any(), any()) }
            }

            /*
             * 토큰 검사가 비밀번호 규칙 검사보다 **먼저**다. 순서가 뒤집히면 아무 토큰이나
             * 넣고 "규칙 위반"이 오는지로 그 토큰이 살아 있는지 떠볼 수 있다.
             */
            it("죽은 토큰에는 비밀번호 규칙을 말해 주지 않는다") {
                every { tokenRepository.getByTokenHash(any()) } returns token(usedAt = now)

                shouldThrow<CoreException> { sut.reset(command.copy(newPassword = "짧음")) }
                    .errorCode shouldBe AuthErrorCode.INVALID_PASSWORD_RESET_TOKEN
            }

            it("살아 있는 토큰이면 비밀번호 규칙을 말해 준다") {
                shouldThrow<CoreException> { sut.reset(command.copy(newPassword = "nodigits")) }
                    .errorCode shouldBe AuthErrorCode.INVALID_PASSWORD

                verify(exactly = 0) { authCredentialRepository.save(any()) }
            }

            it("주인이 사라진 토큰은 거절한다") {
                every { userRepository.getById(owner) } returns null

                shouldThrow<CoreException> { sut.reset(command) }
                    .errorCode shouldBe AuthErrorCode.INVALID_PASSWORD_RESET_TOKEN
            }
        }
    })
