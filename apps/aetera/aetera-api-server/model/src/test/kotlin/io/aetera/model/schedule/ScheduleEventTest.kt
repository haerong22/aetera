package io.aetera.model.schedule

import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant

class ScheduleEventTest :
    DescribeSpec({
        val now = Instant.parse("2026-03-01T09:00:00Z")
        val oneHourLater = now.plusSeconds(3600)

        fun event(
            title: String = "팀 회의",
            startsAt: Instant = now,
            endsAt: Instant = oneHourLater,
            color: String? = null,
        ) = ScheduleEvent.create(
            id = ScheduleEventId.next(),
            userId = UserId.next(),
            title = title,
            description = null,
            startsAt = startsAt,
            endsAt = endsAt,
            allDay = false,
            color = color,
            createdAt = now,
        )

        describe("create") {
            it("제목 앞뒤 공백은 제거된다") {
                event(title = "  팀 회의  ").title shouldBe "팀 회의"
            }

            it("빈 제목은 거절한다") {
                shouldThrow<CoreException> { event(title = "   ") }
                    .errorCode shouldBe ScheduleErrorCode.INVALID_EVENT_TITLE
            }

            it("종료가 시작보다 빠르면 거절한다") {
                shouldThrow<CoreException> { event(startsAt = oneHourLater, endsAt = now) }
                    .errorCode shouldBe ScheduleErrorCode.INVALID_EVENT_PERIOD
            }

            it("시작과 종료가 같은 시점 일정은 허용한다") {
                event(startsAt = now, endsAt = now).endsAt shouldBe now
            }

            it("색상은 #RRGGBB 만 받고 소문자로 통일한다") {
                event(color = "#3182F6").color shouldBe "#3182f6"

                shouldThrow<CoreException> { event(color = "blue") }
                    .errorCode shouldBe ScheduleErrorCode.INVALID_EVENT_COLOR
            }

            it("빈 설명은 null 로 정리한다") {
                event().description.shouldBeNull()
            }
        }

        describe("update") {
            it("검증을 통과한 값으로 통째로 바뀐다") {
                val sut = event()

                sut.update(
                    title = "저녁 약속",
                    description = "강남",
                    startsAt = oneHourLater,
                    endsAt = oneHourLater.plusSeconds(7200),
                    allDay = false,
                    color = "#FF6F0F",
                )

                sut.title shouldBe "저녁 약속"
                sut.color shouldBe "#ff6f0f"
            }

            it("잘못된 기간으로는 바꿀 수 없다") {
                val sut = event()

                shouldThrow<CoreException> {
                    sut.update("x", null, oneHourLater, now, false, null)
                }.errorCode shouldBe ScheduleErrorCode.INVALID_EVENT_PERIOD
            }
        }

        // 겹침 판정 자체는 SQL 이 하므로 여기서 재현하지 않는다 —
        // 경계 동작은 ScheduleEventRepositoryJpaAdapterTest 가 실제 쿼리로 검증한다.
        describe("SchedulePeriod") {
            it("시작이 종료보다 늦으면 거절한다") {
                shouldThrow<CoreException> { SchedulePeriod(oneHourLater, now) }
                    .errorCode shouldBe ScheduleErrorCode.INVALID_SEARCH_PERIOD
            }
        }
    })
