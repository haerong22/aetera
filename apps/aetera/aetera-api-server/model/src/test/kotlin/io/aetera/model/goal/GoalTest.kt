package io.aetera.model.goal

import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.LocalDate

class GoalTest :
    DescribeSpec({
        // 2026-08-24 는 월요일이다. 주 경계를 다루므로 요일이 중요하다.
        val monday = LocalDate.of(2026, 8, 24)
        val now = Instant.parse("2026-08-24T00:00:00Z")

        fun goal(
            title: String = "운동하기",
            period: GoalPeriod = GoalPeriod.WEEKLY,
            target: Int = 3,
            unit: String? = "회",
            today: LocalDate = monday,
        ) = Goal.create(
            id = GoalId.next(),
            userId = UserId.next(),
            title = title,
            period = period,
            target = target,
            unit = unit,
            today = today,
            createdAt = now,
        )

        describe("create") {
            it("진행도는 0 에서 시작한다") {
                goal().progress shouldBe 0
            }

            it("주 목표의 주기 시작은 그 주의 월요일이다") {
                goal(today = LocalDate.of(2026, 8, 27)).periodStart shouldBe monday
            }

            it("월 목표의 주기 시작은 그 달 1일이다") {
                goal(period = GoalPeriod.MONTHLY, today = LocalDate.of(2026, 8, 27))
                    .periodStart shouldBe LocalDate.of(2026, 8, 1)
            }

            it("빈 이름은 거절한다") {
                shouldThrow<CoreException> { goal(title = "   ") }
                    .errorCode shouldBe GoalErrorCode.INVALID_TITLE
            }

            it("목표치는 1 이상이어야 한다") {
                shouldThrow<CoreException> { goal(target = 0) }
                    .errorCode shouldBe GoalErrorCode.INVALID_TARGET
            }
        }

        describe("addProgress") {
            it("쌓으면 목표에 다가간다") {
                val target = goal()

                target.addProgress(1, monday)
                target.addProgress(1, monday)

                target.progress shouldBe 2
                target.isAchieved shouldBe false
            }

            it("목표치에 닿으면 달성이다") {
                val target = goal()

                target.addProgress(3, monday)

                target.isAchieved shouldBe true
            }

            it("목표치를 넘겨도 막지 않는다 — 더 한 것을 못 했다고 할 수 없다") {
                val target = goal()

                target.addProgress(5, monday)

                target.progress shouldBe 5
                target.isAchieved shouldBe true
            }

            it("음수로 되돌릴 수 있다") {
                val target = goal()
                target.addProgress(2, monday)

                target.addProgress(-1, monday)

                target.progress shouldBe 1
            }

            it("되돌려도 0 아래로는 내려가지 않는다") {
                val target = goal()

                target.addProgress(-5, monday)

                target.progress shouldBe 0
            }

            it("주가 바뀌면 0 부터 다시 센다") {
                val target = goal()
                target.addProgress(3, monday)

                val nextMonday = monday.plusWeeks(1)
                target.addProgress(1, nextMonday)

                target.progress shouldBe 1
                target.periodStart shouldBe nextMonday
            }

            it("같은 주 안에서는 리셋되지 않는다") {
                val target = goal()
                target.addProgress(2, monday)

                target.addProgress(1, monday.plusDays(6))

                target.progress shouldBe 3
                target.periodStart shouldBe monday
            }
        }

        describe("update") {
            it("주기를 바꾸면 진행도를 이어받지 않는다 — 재는 창이 달라진다") {
                val target = goal()
                target.addProgress(3, monday)

                target.update("운동하기", GoalPeriod.MONTHLY, 12, "회", monday)

                target.progress shouldBe 0
                target.periodStart shouldBe LocalDate.of(2026, 8, 1)
            }

            it("주기를 그대로 두면 진행도가 남는다") {
                val target = goal()
                target.addProgress(2, monday)

                target.update("운동 더 하기", GoalPeriod.WEEKLY, 5, "회", monday)

                target.progress shouldBe 2
                target.target shouldBe 5
            }
        }
    })
