package io.aetera.usecase.user

import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.PasswordResetTokenRepository
import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.module.ModuleEnrollmentRepository
import io.aetera.model.module.UserDataContributor
import io.aetera.model.notification.NotificationPreferenceRepository
import io.aetera.model.user.Email
import io.aetera.model.user.User
import io.aetera.model.user.UserErrorCode
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.model.user.UserStatus
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.time.ZoneOffset

/**
 * 탈퇴는 되돌릴 수 없다. 그래서 두 가지를 못 박는다 —
 * **하나도 빠뜨리지 않는가**, 그리고 **없는 사람을 지우려 하지 않는가**.
 */
class WithdrawServiceTest :
    DescribeSpec({
        val owner = UserId.next()
        val user =
            User.reconstitute(
                id = owner,
                email = Email("hong@example.com"),
                nickname = "홍길동",
                timezone = ZoneOffset.UTC,
                status = UserStatus.ACTIVE,
                registeredAt = Instant.parse("2026-01-01T00:00:00Z"),
            )

        val userRepository = mockk<UserRepository>(relaxed = true)
        val authCredentialRepository = mockk<AuthCredentialRepository>(relaxed = true)
        val refreshTokenRepository = mockk<RefreshTokenRepository>(relaxed = true)
        val passwordResetTokenRepository = mockk<PasswordResetTokenRepository>(relaxed = true)
        val moduleEnrollmentRepository = mockk<ModuleEnrollmentRepository>(relaxed = true)
        val notificationPreferenceRepository = mockk<NotificationPreferenceRepository>(relaxed = true)

        /** 불렸는지 스스로 기억하는 기여자. */
        class Recording(
            override val section: String,
        ) : UserDataContributor {
            var deleted = false
                private set

            override fun exportFor(userId: UserId) = emptyList<Map<String, Any?>>()

            override fun deleteAllFor(userId: UserId) {
                deleted = true
            }
        }

        fun service(vararg contributors: UserDataContributor) = WithdrawService(
            contributors.toList(),
            userRepository,
            authCredentialRepository,
            refreshTokenRepository,
            passwordResetTokenRepository,
            moduleEnrollmentRepository,
            notificationPreferenceRepository,
        )

        beforeEach {
            every { userRepository.getById(owner) } returns user
        }

        describe("모듈 데이터") {
            /*
             * 이 시험이 지키는 것: 새 모듈이 UserDataContributor 를 구현하면 탈퇴에도
             * 자동으로 걸린다. 빠뜨리면 그 모듈의 돈 이야기만 조용히 남는다.
             */
            it("기여자를 하나도 빠뜨리지 않는다") {
                val asset = Recording("asset")
                val income = Recording("income")
                val guide = Recording("guide")

                service(asset, income, guide).withdraw(owner.value)

                listOf(asset.deleted, income.deleted, guide.deleted)
                    .shouldContainExactlyInAnyOrder(listOf(true, true, true))
            }

            it("기여자가 없어도 계정은 지운다") {
                service().withdraw(owner.value)

                verify { userRepository.delete(user) }
            }
        }

        describe("코어가 들고 있는 것") {
            // 모듈이 아니라 기여자가 없다 — 여기서 빠지면 아무도 안 지운다.
            it("모듈 설정·알림 설정·인증·토큰을 함께 지운다") {
                service().withdraw(owner.value)

                verify { moduleEnrollmentRepository.deleteAllByUserId(owner) }
                verify { notificationPreferenceRepository.deleteAllByUserId(owner) }
                verify { authCredentialRepository.deleteAllByUserId(owner) }
                verify { refreshTokenRepository.deleteAllByUserId(owner) }
                // 재설정 토큰이 남으면 지운 계정의 메일 링크로 다시 비밀번호를 정할 수 있다.
                verify { passwordResetTokenRepository.deleteAllByUserId(owner) }
            }

            /*
             * 상태만 바꾸면 이메일 유니크 인덱스가 그 주소를 묶어 같은 메일로 다시 올 수 없다.
             * "지워 달라"는 말에 "안 보이게 해 두었다"로 답하는 셈이기도 하다.
             */
            it("사용자 행 자체를 지운다 — 상태만 바꾸지 않는다") {
                service().withdraw(owner.value)

                verify { userRepository.delete(user) }
                verify(exactly = 0) { userRepository.save(any()) }
            }
        }

        describe("없는 사용자") {
            it("거절하고 아무것도 지우지 않는다") {
                every { userRepository.getById(owner) } returns null
                val asset = Recording("asset")

                shouldThrow<CoreException> { service(asset).withdraw(owner.value) }
                    .errorCode shouldBe UserErrorCode.USER_NOT_FOUND

                asset.deleted shouldBe false
            }
        }
    })
