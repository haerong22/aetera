package io.aetera.usecase.user

import io.aetera.model.user.Email
import io.aetera.model.user.User
import io.aetera.model.user.UserErrorCode
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.model.user.UserStatus
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant

class ChangeNicknameServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-09-27T00:00:00Z")
        val userRepository = mockk<UserRepository>()
        val sut = ChangeNicknameService(userRepository)

        val owner = UserId.next()

        fun user() = User.reconstitute(
            id = owner,
            email = Email("hong@example.com"),
            nickname = "홍길동",
            timezone = User.DEFAULT_TIMEZONE,
            status = UserStatus.ACTIVE,
            registeredAt = now,
        )

        beforeTest {
            clearMocks(userRepository)
            every { userRepository.getById(owner) } returns user()
            every { userRepository.save(any()) } answers { firstArg() }
        }

        describe("바꾸기") {
            it("새 닉네임을 저장하고 돌려준다") {
                val changed = sut.changeNickname(owner.value, "임꺽정")

                changed.nickname shouldBe "임꺽정"
                verify { userRepository.save(match { it.nickname == "임꺽정" }) }
            }

            // 다듬기는 User 가 한다. 여기서 또 보면 두 곳이 서로 달라질 자리가 생긴다.
            it("앞뒤 공백은 User 가 다듬는다") {
                sut.changeNickname(owner.value, "  임꺽정  ") shouldBe
                    sut.changeNickname(owner.value, "임꺽정")
            }

            it("이메일은 건드리지 않는다") {
                val changed = sut.changeNickname(owner.value, "임꺽정")

                changed.email shouldBe "hong@example.com"
            }
        }

        describe("거절") {
            it("빈 닉네임을 막는다") {
                shouldThrow<CoreException> { sut.changeNickname(owner.value, "   ") }
                    .errorCode shouldBe UserErrorCode.INVALID_NICKNAME

                verify(exactly = 0) { userRepository.save(any()) }
            }

            it("너무 긴 닉네임을 막는다") {
                shouldThrow<CoreException> { sut.changeNickname(owner.value, "가".repeat(31)) }
                    .errorCode shouldBe UserErrorCode.INVALID_NICKNAME

                verify(exactly = 0) { userRepository.save(any()) }
            }

            it("없는 사용자면 거절한다") {
                every { userRepository.getById(owner) } returns null

                shouldThrow<CoreException> { sut.changeNickname(owner.value, "임꺽정") }
                    .errorCode shouldBe UserErrorCode.USER_NOT_FOUND
            }
        }
    })
