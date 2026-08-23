package io.aetera.usecase.schedule

import io.aetera.model.schedule.ScheduleErrorCode
import io.aetera.model.schedule.ScheduleEventId
import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.orNotFound
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetScheduleEventService(
    private val scheduleEventRepository: ScheduleEventRepository,
) {
    fun getEvent(
        userId: UUID,
        eventId: UUID,
    ): ScheduleEventDto {
        val id = ScheduleEventId(eventId)
        val event = scheduleEventRepository.getById(id).orNotFound(UserId(userId), ScheduleErrorCode.EVENT_NOT_FOUND, id)
        return ScheduleEventDto(event)
    }
}
