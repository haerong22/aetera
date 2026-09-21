package io.aetera.usecase.goal

import io.aetera.model.goal.Goal
import io.aetera.model.goal.GoalPeriod
import java.time.LocalDate
import java.util.UUID

/**
 * 화면이 보는 목표 한 줄.
 *
 * **[today] 를 받는다.** 목표는 주기가 넘어가도 다음 기록 전까지 저장된 값이 지난 주기의
 * 것이라, 날짜 없이 만들면 "지난주에 다 한 목표"가 이번 주에도 이룬 것으로 나간다.
 */
data class GoalDto(
    val id: UUID,
    val title: String,
    val period: GoalPeriod,
    val target: Int,
    val unit: String?,
    val progress: Int,
    val periodStart: LocalDate,
    val achieved: Boolean,
) {
    constructor(goal: Goal, today: LocalDate) : this(
        id = goal.id.value,
        title = goal.title,
        period = goal.period,
        target = goal.target,
        unit = goal.unit,
        progress = goal.progressOn(today),
        periodStart = goal.period.startOf(today),
        achieved = goal.isAchievedOn(today),
    )
}
