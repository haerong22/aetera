package io.aetera.usecase.schedule

import io.aetera.model.schedule.ScheduleEventId
import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.user.UserId
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
    ): ScheduleEventDto = ScheduleEventDto(
        scheduleEventRepository.getOwnedOrThrow(ScheduleEventId(eventId), UserId(userId)),
    )
}
