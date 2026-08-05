package io.aetera.model.schedule

import io.aetera.model.user.UserId

interface ScheduleEventRepository {
    fun save(event: ScheduleEvent): ScheduleEvent

    fun getById(id: ScheduleEventId): ScheduleEvent?

    /** 기간과 겹치는 일정을 시작 시각 오름차순으로. */
    fun findAllOverlapping(
        userId: UserId,
        period: SchedulePeriod,
    ): List<ScheduleEvent>

    fun delete(event: ScheduleEvent)
}
