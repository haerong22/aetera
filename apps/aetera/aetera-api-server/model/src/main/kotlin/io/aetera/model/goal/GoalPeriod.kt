package io.aetera.model.goal

import java.time.LocalDate
import java.time.temporal.WeekFields

/**
 * 목표를 재는 단위. 주기가 바뀌면 진행도가 0 부터 다시 시작한다.
 *
 * "이번 주에 몇 번 했는가"가 목표의 본질이라, 주기를 넘기면 지난 성적을 들고 가지 않는다.
 */
enum class GoalPeriod {
    WEEKLY,
    MONTHLY,
    ;

    /**
     * 이 날짜가 속한 주기의 시작일. 진행도를 언제 리셋할지 판단하는 기준이 된다.
     *
     * 주는 월요일 시작으로 고정한다 — 로케일에 맡기면 서버와 브라우저가 다른 주를 볼 수 있다.
     */
    fun startOf(date: LocalDate): LocalDate = when (this) {
        WEEKLY -> date.with(WeekFields.ISO.dayOfWeek(), 1)
        MONTHLY -> date.withDayOfMonth(1)
    }
}
