package io.aetera.model.renewal

import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.LocalDate

class RenewalTest :
    DescribeSpec({
        val today = LocalDate.of(2026, 8, 21)
        val now = Instant.parse("2026-08-21T00:00:00Z")

        fun renewal(
            title: String = "자동차보험",
            expiresAt: LocalDate = LocalDate.of(2026, 9, 30),
            cycle: RenewalCycle = RenewalCycle.YEARLY,
            noticeDays: Int = 30,
            memo: String? = null,
        ) = Renewal.create(
            id = RenewalId.next(),
            userId = UserId.next(),
            title = title,
            category = RenewalCategory.INSURANCE,
            expiresAt = expiresAt,
            cycle = cycle,
            noticeDays = noticeDays,
            memo = memo,
            today = today,
            createdAt = now,
        )

        describe("create") {
            it("이름 앞뒤 공백은 제거된다") {
                renewal(title = "  자동차보험  ").title shouldBe "자동차보험"
            }

            it("빈 이름은 거절한다") {
                shouldThrow<CoreException> { renewal(title = "   ") }
                    .errorCode shouldBe RenewalErrorCode.INVALID_TITLE
            }

            it("공백뿐인 메모는 없는 것으로 본다") {
                renewal(memo = "   ").memo.shouldBeNull()
            }

            it("상식 밖으로 먼 만기일은 거절한다") {
                shouldThrow<CoreException> { renewal(expiresAt = LocalDate.of(2206, 9, 30)) }
                    .errorCode shouldBe RenewalErrorCode.INVALID_EXPIRY_DATE
            }

            it("미리 알림 일수가 범위를 벗어나면 거절한다") {
                shouldThrow<CoreException> { renewal(noticeDays = -1) }
                    .errorCode shouldBe RenewalErrorCode.INVALID_NOTICE_DAYS
                shouldThrow<CoreException> { renewal(noticeDays = 366) }
                    .errorCode shouldBe RenewalErrorCode.INVALID_NOTICE_DAYS
            }
        }

        describe("renew") {
            it("만기 전에 미리 갱신하면 기존 만기부터 이어진다") {
                val target = renewal(expiresAt = LocalDate.of(2026, 9, 30))

                target.renew(LocalDate.of(2026, 9, 1))

                target.expiresAt shouldBe LocalDate.of(2027, 9, 30)
            }

            it("늦게 갱신하면 갱신한 날부터 시작한다") {
                val target = renewal(expiresAt = LocalDate.of(2026, 8, 1))

                target.renew(LocalDate.of(2026, 8, 21))

                target.expiresAt shouldBe LocalDate.of(2027, 8, 21)
            }

            it("주기가 짧으면 그만큼만 굴러간다") {
                val target = renewal(expiresAt = LocalDate.of(2026, 9, 30), cycle = RenewalCycle.QUARTERLY)

                target.renew(LocalDate.of(2026, 9, 1))

                target.expiresAt shouldBe LocalDate.of(2026, 12, 30)
            }

            it("여러 번 갱신하면 주기만큼 계속 쌓인다") {
                val target = renewal(expiresAt = LocalDate.of(2026, 9, 30))

                target.renew(LocalDate.of(2026, 9, 1))
                target.renew(LocalDate.of(2026, 9, 1))

                target.expiresAt shouldBe LocalDate.of(2028, 9, 30)
            }

            it("주기가 없으면 갱신할 수 없다") {
                val target = renewal(cycle = RenewalCycle.NONE)

                shouldThrow<CoreException> { target.renew(today) }
                    .errorCode shouldBe RenewalErrorCode.CYCLE_NOT_REPEATABLE
            }
        }
    })
