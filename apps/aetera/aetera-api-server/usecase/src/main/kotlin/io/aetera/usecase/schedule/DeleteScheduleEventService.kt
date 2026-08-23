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
class DeleteScheduleEventService(
    private val scheduleEventRepository: ScheduleEventRepository,
) {
    @Transactional
    fun delete(
        userId: UUID,
        eventId: UUID,
    ) {
        val id = ScheduleEventId(eventId)
        val event = scheduleEventRepository.getById(id).orNotFound(UserId(userId), ScheduleErrorCode.EVENT_NOT_FOUND, id)
        scheduleEventRepository.delete(event)
    }
}
