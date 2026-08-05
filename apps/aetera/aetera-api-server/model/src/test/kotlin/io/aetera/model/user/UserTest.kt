package io.aetera.model.user

import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.ZoneId

class UserTest :
    DescribeSpec({
        val now = Instant.parse("2026-01-01T00:00:00Z")

        fun user(nickname: String = "홍길동") = User.register(
            id = UserId.next(),
            email = Email("hong@example.com"),
            nickname = nickname,
            registeredAt = now,
        )

        describe("register") {
            it("가입 직후에는 ACTIVE 상태이고 기본 타임존은 서울이다") {
                val sut = user()

                sut.status shouldBe UserStatus.ACTIVE
                sut.timezone shouldBe ZoneId.of("Asia/Seoul")
            }

            it("닉네임 앞뒤 공백은 제거된다") {
                user("  홍길동  ").nickname shouldBe "홍길동"
            }

            it("공백 닉네임은 거절한다") {
                shouldThrow<CoreException> { user("   ") }.errorCode shouldBe UserErrorCode.INVALID_NICKNAME
            }

            it("30자를 넘는 닉네임은 거절한다") {
                shouldThrow<CoreException> { user("가".repeat(31)) }.errorCode shouldBe UserErrorCode.INVALID_NICKNAME
            }
        }

        describe("Email") {
            it("형식이 틀린 주소는 거절한다") {
                shouldThrow<CoreException> { Email("not-an-email") }.errorCode shouldBe UserErrorCode.INVALID_EMAIL
            }

            // 정규화하지 않으면 같은 메일함으로 계정이 두 개 생기고,
            // 대소문자를 다르게 입력한 사용자는 로그인에 실패한다.
            it("대소문자와 앞뒤 공백을 정규화해서 같은 주소로 취급한다") {
                Email("  Hong@Example.COM ") shouldBe Email("hong@example.com")
                Email("Hong@Example.COM").value shouldBe "hong@example.com"
            }
        }

        describe("parseTimezone") {
            it("올바른 타임존을 해석한다") {
                User.parseTimezone("Asia/Tokyo") shouldBe ZoneId.of("Asia/Tokyo")
            }

            it("엉터리 타임존은 거절한다") {
                shouldThrow<CoreException> { User.parseTimezone("Mars/Olympus") }
                    .errorCode shouldBe UserErrorCode.INVALID_TIMEZONE
            }
        }

        describe("withdraw") {
            it("탈퇴 시각을 기록하고 종료 상태가 된다") {
                val sut = user()

                sut.withdraw(now)

                sut.status shouldBe UserStatus.WITHDRAWN
                sut.withdrawnAt shouldBe now
            }

            it("두 번 탈퇴할 수 없다") {
                val sut = user()
                sut.withdraw(now)

                shouldThrow<CoreException> { sut.withdraw(now) }
                    .errorCode shouldBe UserErrorCode.USER_ALREADY_WITHDRAWN
            }

            it("탈퇴 후에는 닉네임을 바꿀 수 없다") {
                val sut = user()
                sut.withdraw(now)

                shouldThrow<CoreException> { sut.changeNickname("새이름") }
            }
        }
    })
