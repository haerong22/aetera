package io.aetera.model.expense

import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class FixedExpenseTest :
    DescribeSpec({
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val userId = UserId.next()

        fun expense(
            title: String = "월세",
            amount: Long = 700_000L,
            cycle: ExpenseCycle = ExpenseCycle.MONTHLY,
            memo: String? = null,
        ) = FixedExpense.create(
            id = FixedExpenseId.next(),
            userId = userId,
            title = title,
            category = ExpenseCategory.HOUSING,
            amount = amount,
            cycle = cycle,
            memo = memo,
            createdAt = now,
        )

        describe("연 환산") {
            it("월 결제는 12배다") {
                expense(amount = 700_000L).yearlyAmount shouldBe 8_400_000L
            }

            it("분기 결제는 4배다") {
                expense(amount = 300_000L, cycle = ExpenseCycle.QUARTERLY).yearlyAmount shouldBe 1_200_000L
            }

            it("연 결제는 그대로다") {
                expense(amount = 120_000L, cycle = ExpenseCycle.YEARLY).yearlyAmount shouldBe 120_000L
            }
        }

        describe("합계") {
            it("주기가 섞여 있어도 한 달치로 모은다") {
                val expenses =
                    listOf(
                        expense(amount = 700_000L), // 월 70만 → 연 840만
                        expense(amount = 300_000L, cycle = ExpenseCycle.QUARTERLY), // 연 120만
                        expense(amount = 120_000L, cycle = ExpenseCycle.YEARLY), // 연 12만
                    )

                expenses.yearlyTotal() shouldBe 9_720_000L
                expenses.monthlyTotal() shouldBe 810_000L
            }

            // 항목마다 월로 내려 더하면 1원씩 버려진다. 연으로 합친 뒤 한 번만 나눠야
            // 화면의 금액을 손으로 더한 값과 합계가 맞는다.
            it("나누어떨어지지 않아도 원이 새지 않는다") {
                val expenses = List(3) { expense(amount = 10_000L, cycle = ExpenseCycle.QUARTERLY) }

                // 항목마다 월 환산하면 3,333 × 3 = 9,999 로 1원이 사라진다.
                expenses.monthlyTotal() shouldBe 10_000L
            }

            it("항목이 없으면 0 이다") {
                emptyList<FixedExpense>().monthlyTotal() shouldBe 0L
            }
        }

        describe("수정") {
            it("주기를 바꾸면 연 환산이 따라 바뀐다") {
                val target = expense(amount = 120_000L, cycle = ExpenseCycle.MONTHLY)

                target.update("보험료", ExpenseCategory.INSURANCE, 120_000L, ExpenseCycle.YEARLY, null)

                target.yearlyAmount shouldBe 120_000L
                target.category shouldBe ExpenseCategory.INSURANCE
            }
        }

        describe("입력 검증") {
            it("빈 이름은 거절한다") {
                shouldThrow<CoreException> { expense(title = "   ") }
                    .errorCode shouldBe ExpenseErrorCode.INVALID_TITLE
            }

            it("금액은 1원 이상이어야 한다") {
                shouldThrow<CoreException> { expense(amount = 0L) }
                    .errorCode shouldBe ExpenseErrorCode.INVALID_AMOUNT
            }

            it("터무니없이 큰 금액은 거절한다 — 0 을 더 붙인 실수를 걸러낸다") {
                shouldThrow<CoreException> { expense(amount = 1_000_000_001L) }
                    .errorCode shouldBe ExpenseErrorCode.INVALID_AMOUNT
            }

            it("긴 메모는 거절한다") {
                shouldThrow<CoreException> { expense(memo = "가".repeat(501)) }
                    .errorCode shouldBe ExpenseErrorCode.MEMO_TOO_LONG
            }

            it("빈 메모는 없는 것으로 본다") {
                expense(memo = "   ").memo shouldBe null
            }
        }
    })
