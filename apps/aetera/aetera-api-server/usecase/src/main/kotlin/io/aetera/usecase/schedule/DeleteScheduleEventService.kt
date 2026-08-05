package io.aetera.usecase.schedule

import io.aetera.model.schedule.ScheduleEventId
import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.user.UserId
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
        val event = scheduleEventRepository.getOwnedOrThrow(ScheduleEventId(eventId), UserId(userId))
        scheduleEventRepository.delete(event)
    }
}
