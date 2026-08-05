package io.aetera.usecase.schedule

import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.schedule.cmd.FindScheduleEventsCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FindScheduleEventsService(
    private val scheduleEventRepository: ScheduleEventRepository,
) {
    /** 조회 기간과 겹치는 일정 전부 — 캘린더 화면 하나가 이 호출 한 번으로 그려진다. */
    fun findEvents(command: FindScheduleEventsCommand): List<ScheduleEventDto> = scheduleEventRepository
        .findAllOverlapping(UserId(command.userId), command.toPeriod())
        .map(::ScheduleEventDto)
}
