package io.aetera.usecase.goal

import io.aetera.model.goal.Goal
import io.aetera.model.goal.GoalId
import io.aetera.model.goal.GoalPeriod
import io.aetera.model.goal.GoalRepository
import io.aetera.model.user.UserId
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.time.LocalDate

/**
 * **이 기여자만 미리 알린다.** 만기·가이드는 지났을 때 재촉하면 되지만, 목표는 주기가
 * 넘어가면 진행도가 0 으로 돌아가 재촉할 것 자체가 사라진다.
 *
 * 그래서 경계가 중요하다 — 너무 일찍 보내면 주중 내내 오고, 늦으면 이미 지난 얘기다.
 */
class GoalNotificationContributorTest :
    DescribeSpec({
        val goalRepository = mockk<GoalRepository>()
        val sut = GoalNotificationContributor(goalRepository)

        val owner = UserId.next()

        // 2026-09-21 은 월요일. 그 주는 27일(일요일)에 끝난다.
        val monday = LocalDate.of(2026, 9, 21)
        val saturday = monday.plusDays(5)
        val sunday = monday.plusDays(6)

        fun goal(
            title: String = "운동하기",
            period: GoalPeriod = GoalPeriod.WEEKLY,
            target: Int = 3,
            unit: String? = null,
            progress: Int = 1,
            periodStart: LocalDate = monday,
        ) = Goal.reconstitute(
            id = GoalId.next(),
            userId = owner,
            title = title,
            period = period,
            target = target,
            unit = unit,
            progress = progress,
            periodStart = periodStart,
            createdAt = Instant.parse("2026-09-01T00:00:00Z"),
        )

        fun goals(vararg goals: Goal) {
            every { goalRepository.findAllByUserId(owner) } returns goals.toList()
        }

        describe("주기가 끝날 때만") {
            it("주중에는 조용하다") {
                goals(goal())

                sut.noticesFor(owner, monday.plusDays(3)).shouldBeEmpty()
            }

            it("마지막 전날부터 알린다") {
                goals(goal())

                sut.noticesFor(owner, saturday).first().detail shouldContain "내일까지"
            }

            it("마지막 날에도 알린다") {
                goals(goal())

                sut.noticesFor(owner, sunday).first().detail shouldContain "오늘까지"
            }
        }

        describe("남은 양") {
            it("목표에서 진행도를 뺀 만큼") {
                goals(goal(target = 3, progress = 1))

                sut.noticesFor(owner, sunday).first().detail shouldContain "2회 남았어요"
            }

            it("단위를 정했으면 그 단위로") {
                goals(goal(target = 10, progress = 4, unit = "km"))

                sut.noticesFor(owner, sunday).first().detail shouldContain "6km 남았어요"
            }
        }

        describe("이룬 목표는 빼고") {
            it("채웠으면 재촉하지 않는다") {
                goals(goal(target = 3, progress = 3))

                sut.noticesFor(owner, sunday).shouldBeEmpty()
            }

            it("넘겼어도 마찬가지") {
                goals(goal(target = 3, progress = 5))

                sut.noticesFor(owner, sunday).shouldBeEmpty()
            }
        }

        describe("주기가 넘어간 목표") {
            /*
             * 지난주에 3/3 을 채우고 이번 주에 아무도 기록하지 않았으면 DB 에는 progress=3 이
             * 그대로 남아 있다. 그걸 믿으면 "이룬 목표"로 보고 조용해진다 —
             * 실제로는 이번 주에 아직 0 이다.
             */
            it("지난 주기의 성적을 이번 주기로 들고 오지 않는다") {
                goals(goal(target = 3, progress = 3, periodStart = monday.minusDays(7)))

                val notices = sut.noticesFor(owner, sunday)

                notices shouldHaveSize 1
                notices.first().detail shouldContain "3회 남았어요"
            }
        }

        describe("월간 목표") {
            it("월말 전날부터 알린다") {
                val septemberGoal = goal(period = GoalPeriod.MONTHLY, periodStart = LocalDate.of(2026, 9, 1))
                goals(septemberGoal)

                sut.noticesFor(owner, LocalDate.of(2026, 9, 28)).shouldBeEmpty()
                sut.noticesFor(owner, LocalDate.of(2026, 9, 29)).first().detail shouldContain "내일까지"
                sut.noticesFor(owner, LocalDate.of(2026, 9, 30)).first().detail shouldContain "오늘까지"
            }
        }

        describe("여러 개") {
            it("못 채운 것만 모아 낸다") {
                goals(
                    goal(title = "운동하기", target = 3, progress = 1),
                    goal(title = "책 읽기", target = 2, progress = 2),
                    goal(title = "물 마시기", target = 7, progress = 5),
                )

                sut.noticesFor(owner, sunday).map { it.title } shouldBe listOf("운동하기", "물 마시기")
            }
        }
    })
