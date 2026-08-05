package io.aetera.usecase.schedule

import io.aetera.model.schedule.ScheduleErrorCode
import io.aetera.model.schedule.ScheduleEvent
import io.aetera.model.schedule.ScheduleEventId
import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.aetera.usecase.schedule.cmd.UpdateScheduleEventCommand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant

class UpdateScheduleEventServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-03-01T09:00:00Z")
        val repository = mockk<ScheduleEventRepository>()
        val sut = UpdateScheduleEventService(repository)

        val owner = UserId.next()
        val stranger = UserId.next()

        fun event() = ScheduleEvent.create(
            id = ScheduleEventId.next(),
            userId = owner,
            title = "팀 회의",
            description = null,
            startsAt = now,
            endsAt = now.plusSeconds(3600),
            allDay = false,
            color = null,
            createdAt = now,
        )

        fun command(
            requester: UserId,
            eventId: ScheduleEventId,
        ) = UpdateScheduleEventCommand(
            userId = requester.value,
            eventId = eventId.value,
            title = "저녁 약속",
            description = "강남",
            startsAt = now.plusSeconds(7200),
            endsAt = now.plusSeconds(10_800),
            allDay = false,
            color = "#3182f6",
        )

        beforeTest {
            clearMocks(repository)
            every { repository.save(any()) } answers { firstArg() }
        }

        it("소유자는 일정을 수정할 수 있다") {
            val target = event()
            every { repository.getById(target.id) } returns target

            val result = sut.update(command(owner, target.id))

            result.title shouldBe "저녁 약속"
            result.color shouldBe "#3182f6"
        }

        it("남의 일정은 존재 여부조차 알려주지 않는다") {
            val target = event()
            every { repository.getById(target.id) } returns target

            shouldThrow<CoreException> { sut.update(command(stranger, target.id)) }
                .errorCode shouldBe ScheduleErrorCode.EVENT_NOT_FOUND
            verify(exactly = 0) { repository.save(any()) }
        }

        it("없는 일정이면 EVENT_NOT_FOUND 로 실패한다") {
            every { repository.getById(any()) } returns null

            shouldThrow<CoreException> { sut.update(command(owner, ScheduleEventId.next())) }
                .errorCode shouldBe ScheduleErrorCode.EVENT_NOT_FOUND
        }
    })
