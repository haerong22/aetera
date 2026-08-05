package io.aetera.usecase.schedule

import io.aetera.model.schedule.ScheduleErrorCode
import io.aetera.model.schedule.ScheduleEvent
import io.aetera.model.schedule.ScheduleEventId
import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException

/**
 * 남의 일정은 존재 여부조차 알려주지 않는다 — 없는 것과 남의 것을 같은 코드로 거절한다.
 */
internal fun ScheduleEventRepository.getOwnedOrThrow(
    id: ScheduleEventId,
    owner: UserId,
): ScheduleEvent {
    val event = getById(id)
    if (event == null || event.userId != owner) {
        throw CoreException(ScheduleErrorCode.EVENT_NOT_FOUND, "일정을 찾을 수 없습니다. id=$id")
    }
    return event
}
