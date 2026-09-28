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
import io.aetera.model.user.UserErrorCode
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.model.user.UserStatus
import io.aetera.shared.error.CoreException
import io.aetera.usecase.auth.cmd.ChangePasswordCommand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * 비밀번호 변경에서 진짜 중요한 것은 "해시가 바뀌었는가"가 아니라 **다른 기기가 끊겼는가**다.
 *
 * 남이 이미 로그인해 두었다면 그 세션의 리프레시 토큰은 비밀번호와 무관하게 살아 있다.
 * 끊지 않으면 잠금장치만 바꾸고 침입자를 안에 두는 셈인데, 화면은 "변경되었습니다"라고
 * 말하므로 **아무도 모른다.**
 */
class ChangePasswordServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-09-27T00:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)

        val userRepository = mockk<UserRepository>()
        val authCredentialRepository = mockk<AuthCredentialRepository>(relaxed = true)
        val refreshTokenRepository = mockk<RefreshTokenRepository>(relaxed = true)
        val passwordEncryptor = mockk<PasswordEncryptor>()
        val accessTokenProvider = mockk<AccessTokenProvider>()
        val sessionIssuer = SessionIssuer(accessTokenProvider, refreshTokenRepository, clock)
        val sut =
            ChangePasswordService(
                userRepository,
                authCredentialRepository,
                refreshTokenRepository,
                passwordEncryptor,
                sessionIssuer,
                clock,
            )

        val owner = UserId.next()
        val currentHash = EncryptedPassword("현재-해시")
        val newHash = EncryptedPassword("새-해시")

        val user =
            User.reconstitute(
                id = owner,
                email = Email("hong@example.com"),
                nickname = "홍길동",
                timezone = User.DEFAULT_TIMEZONE,
                status = UserStatus.ACTIVE,
                registeredAt = now.minusSeconds(86_400),
            )

        fun emailCredential() = AuthCredential.email(AuthCredentialId.next(), owner, currentHash, now)

        val command = ChangePasswordCommand(owner.value, "password1234", "newpassword5678")

        beforeTest {
            clearMocks(userRepository, authCredentialRepository, refreshTokenRepository, passwordEncryptor, accessTokenProvider)
            every { userRepository.getById(owner) } returns user
            every { authCredentialRepository.getByUserIdAndProvider(owner, AuthProvider.EMAIL) } returns emailCredential()
            every { authCredentialRepository.save(any()) } answers { firstArg() }
            every { refreshTokenRepository.save(any()) } answers { firstArg() }
            every { accessTokenProvider.issue(owner) } returns IssuedAccessToken("access-token", 900)
            every { passwordEncryptor.matches("password1234", currentHash) } returns true
            every { passwordEncryptor.matches("newpassword5678", currentHash) } returns false
            every { passwordEncryptor.encrypt("newpassword5678") } returns newHash
        }

        describe("바꾸기") {
            it("새 해시를 저장한다") {
                sut.changePassword(command)

                verify { authCredentialRepository.save(match { it.passwordHash == newHash }) }
            }

            it("다른 기기의 세션을 모두 끊는다") {
                sut.changePassword(command)

                verify { refreshTokenRepository.revokeAllByUserId(owner, now) }
            }

            /*
             * 순서가 뒤집히면 방금 발급한 토큰까지 같이 끊긴다. 그러면 비밀번호를 바꾸자마자
             * 자기도 로그아웃되는데, 화면에는 성공이라고 떠서 원인을 찾기 어렵다.
             */
            it("끊은 다음에 새로 발급한다") {
                sut.changePassword(command)

                verifyOrder {
                    refreshTokenRepository.revokeAllByUserId(owner, now)
                    refreshTokenRepository.save(any())
                }
            }

            it("이 기기 몫의 새 세션을 돌려준다") {
                val session = sut.changePassword(command)

                session.accessToken shouldBe "access-token"
                session.refreshToken.isBlank() shouldBe false
            }
        }

        describe("거절") {
            it("지금 비밀번호가 틀리면 바꾸지 않는다") {
                every { passwordEncryptor.matches("password1234", currentHash) } returns false

                shouldThrow<CoreException> { sut.changePassword(command) }
                    .errorCode shouldBe AuthErrorCode.CURRENT_PASSWORD_MISMATCH

                verify(exactly = 0) { authCredentialRepository.save(any()) }
                verify(exactly = 0) { refreshTokenRepository.revokeAllByUserId(any(), any()) }
            }

            /*
             * 로그인 실패와 달리 무엇이 틀렸는지 알려 준다 — 이미 로그인한 사람이라 가입 여부가
             * 샐 것이 없고, 숨기면 "새 비밀번호가 규칙에 안 맞는 것"과 구분이 안 된다.
             */
            it("지금 비밀번호가 틀린 것과 새 비밀번호가 나쁜 것을 다른 코드로 답한다") {
                every { passwordEncryptor.matches("password1234", currentHash) } returns false
                val wrongCurrent =
                    shouldThrow<CoreException> { sut.changePassword(command) }.errorCode

                every { passwordEncryptor.matches("password1234", currentHash) } returns true
                val badNew =
                    shouldThrow<CoreException> {
                        sut.changePassword(command.copy(newPassword = "short1"))
                    }.errorCode

                wrongCurrent shouldBe AuthErrorCode.CURRENT_PASSWORD_MISMATCH
                badNew shouldBe AuthErrorCode.INVALID_PASSWORD
            }

            it("규칙에 안 맞는 새 비밀번호를 막는다") {
                shouldThrow<CoreException> {
                    sut.changePassword(command.copy(newPassword = "nodigitshere"))
                }.errorCode shouldBe AuthErrorCode.INVALID_PASSWORD

                verify(exactly = 0) { authCredentialRepository.save(any()) }
            }

            /*
             * 같은 값으로 끝내면 바꾼 것이 없는데 바꿨다고 답하게 된다. 남이 알아 버렸다고
             * 의심해서 온 사람이라면 위험은 그대로인데 처리된 줄 안다.
             */
            it("지금 것과 같은 비밀번호를 막는다") {
                every { passwordEncryptor.matches("newpassword5678", currentHash) } returns true

                shouldThrow<CoreException> { sut.changePassword(command) }
                    .errorCode shouldBe AuthErrorCode.SAME_AS_CURRENT_PASSWORD

                verify(exactly = 0) { authCredentialRepository.save(any()) }
                verify(exactly = 0) { refreshTokenRepository.revokeAllByUserId(any(), any()) }
            }

            it("없는 사용자면 거절한다") {
                every { userRepository.getById(owner) } returns null

                shouldThrow<CoreException> { sut.changePassword(command) }
                    .errorCode shouldBe UserErrorCode.USER_NOT_FOUND
            }

            // 카카오 등으로만 가입한 계정에는 바꿀 자리가 없다. 지금은 이메일 가입뿐이라 닿지 않는 길이다.
            it("비밀번호 수단이 없는 계정은 거절한다") {
                every { authCredentialRepository.getByUserIdAndProvider(owner, AuthProvider.EMAIL) } returns null

                shouldThrow<CoreException> { sut.changePassword(command) }
                    .errorCode shouldBe AuthErrorCode.PASSWORD_LOGIN_NOT_AVAILABLE
            }
        }
    })
