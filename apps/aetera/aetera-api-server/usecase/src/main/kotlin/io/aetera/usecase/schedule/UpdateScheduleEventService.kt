package io.aetera.usecase.schedule

import io.aetera.model.schedule.ScheduleEventId
import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.schedule.cmd.UpdateScheduleEventCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateScheduleEventService(
    private val scheduleEventRepository: ScheduleEventRepository,
) {
    @Transactional
    fun update(command: UpdateScheduleEventCommand): ScheduleEventDto {
        val event = scheduleEventRepository.getOwnedOrThrow(ScheduleEventId(command.eventId), UserId(command.userId))
        event.update(
            title = command.title,
            description = command.description,
            startsAt = command.startsAt,
            endsAt = command.endsAt,
            allDay = command.allDay,
            color = command.color,
        )
        return ScheduleEventDto(scheduleEventRepository.save(event))
    }
}
