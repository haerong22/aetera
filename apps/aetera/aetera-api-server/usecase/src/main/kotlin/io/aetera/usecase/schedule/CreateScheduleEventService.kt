package io.aetera.usecase.schedule

import io.aetera.model.schedule.ScheduleEvent
import io.aetera.model.schedule.ScheduleEventId
import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.schedule.cmd.CreateScheduleEventCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CreateScheduleEventService(
    private val scheduleEventRepository: ScheduleEventRepository,
    private val clock: Clock,
) {
    @Transactional
    fun create(command: CreateScheduleEventCommand): ScheduleEventDto {
        val event =
            ScheduleEvent.create(
                id = ScheduleEventId.next(),
                userId = UserId(command.userId),
                title = command.title,
                description = command.description,
                startsAt = command.startsAt,
                endsAt = command.endsAt,
                allDay = command.allDay,
                color = command.color,
                createdAt = clock.instant(),
            )
        return ScheduleEventDto(scheduleEventRepository.save(event))
    }
}
