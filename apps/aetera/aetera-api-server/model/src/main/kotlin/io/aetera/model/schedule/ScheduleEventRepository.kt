package io.aetera.model.schedule

import io.aetera.model.user.UserId

interface ScheduleEventRepository {
    fun save(event: ScheduleEvent): ScheduleEvent

    fun getById(id: ScheduleEventId): ScheduleEvent?

    /**
     * 이 사용자의 일정 전부. 내보내기가 쓴다 — 기간을 정할 수 없는 유일한 경우다.
     *
     * 화면은 늘 보이는 기간만 읽으므로 이 메서드를 부르지 않는다.
     */
    fun findAllByUserId(userId: UserId): List<ScheduleEvent>

    /** 기간과 겹치는 일정을 시작 시각 오름차순으로. */
    fun findAllOverlapping(
        userId: UserId,
        period: SchedulePeriod,
    ): List<ScheduleEvent>

    /** 탈퇴할 때 한 번. 이 사용자의 것을 통째로 지운다. */
    fun deleteAllByUserId(userId: UserId)

    fun delete(event: ScheduleEvent)
}
