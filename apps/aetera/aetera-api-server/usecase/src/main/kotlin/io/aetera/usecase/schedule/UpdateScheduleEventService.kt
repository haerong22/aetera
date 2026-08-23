package io.aetera.usecase.schedule

import io.aetera.model.schedule.ScheduleErrorCode
import io.aetera.model.schedule.ScheduleEventId
import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.orNotFound
import io.aetera.usecase.schedule.cmd.UpdateScheduleEventCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateScheduleEventService(
    private val scheduleEventRepository: ScheduleEventRepository,
) {
    @Transactional
    fun update(command: UpdateScheduleEventCommand): ScheduleEventDto {
        val id = ScheduleEventId(command.eventId)
        val event = scheduleEventRepository.getById(id).orNotFound(UserId(command.userId), ScheduleErrorCode.EVENT_NOT_FOUND, id)
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
