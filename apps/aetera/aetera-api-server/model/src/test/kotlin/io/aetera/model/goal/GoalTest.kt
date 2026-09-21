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
                target.isAchievedOn(monday) shouldBe false
            }

            it("목표치에 닿으면 달성이다") {
                val target = goal()

                target.addProgress(3, monday)

                target.isAchievedOn(monday) shouldBe true
            }

            it("목표치를 넘겨도 막지 않는다 — 더 한 것을 못 했다고 할 수 없다") {
                val target = goal()

                target.addProgress(5, monday)

                target.progress shouldBe 5
                target.isAchievedOn(monday) shouldBe true
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

        describe("주기의 끝") {
            // 주는 월요일 시작. 2026-09-21 이 월요일이다.
            it("주간은 그 주 일요일") {
                GoalPeriod.WEEKLY.endOf(LocalDate.of(2026, 9, 21)) shouldBe LocalDate.of(2026, 9, 27)
                GoalPeriod.WEEKLY.endOf(LocalDate.of(2026, 9, 27)) shouldBe LocalDate.of(2026, 9, 27)
            }

            it("월간은 그 달 말일 — 달마다 길이가 다르다") {
                GoalPeriod.MONTHLY.endOf(LocalDate.of(2026, 9, 1)) shouldBe LocalDate.of(2026, 9, 30)
                GoalPeriod.MONTHLY.endOf(LocalDate.of(2026, 2, 15)) shouldBe LocalDate.of(2026, 2, 28)
            }
        }

        describe("저장 없이 보는 진행도") {
            // 쓰기 트랜잭션 안에서 rollOverIfNeeded 를 부르면 더티 체킹이 리셋을 저장해 버린다.
            it("주기가 넘어갔으면 0 으로 보이되 값은 그대로 남는다") {
                val thisWeek = LocalDate.of(2026, 9, 14)
                val nextWeek = LocalDate.of(2026, 9, 21)
                val target = goal(today = thisWeek).apply { addProgress(3, thisWeek) }

                target.progressOn(nextWeek) shouldBe 0
                target.isAchievedOn(nextWeek) shouldBe false
                // 묻기만 했으므로 저장된 값은 건드리지 않는다
                target.progress shouldBe 3
            }

            it("같은 주기면 저장된 값 그대로") {
                val start = LocalDate.of(2026, 9, 21)
                val target = goal(today = start).apply { addProgress(2, start) }

                target.progressOn(start.plusDays(3)) shouldBe 2
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
