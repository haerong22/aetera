package io.aetera.model.schedule

import io.aetera.shared.error.ensure
import java.time.Instant

/** 캘린더 화면의 조회 범위. `[from, to]` 와 겹치는 일정을 모두 찾는다. */
data class SchedulePeriod(
    val from: Instant,
    val to: Instant,
) {
    init {
        ensure(
            !from.isAfter(to),
            ScheduleErrorCode.INVALID_SEARCH_PERIOD,
            "조회 시작 시각이 종료 시각보다 늦을 수 없습니다.",
        )
    }
}
