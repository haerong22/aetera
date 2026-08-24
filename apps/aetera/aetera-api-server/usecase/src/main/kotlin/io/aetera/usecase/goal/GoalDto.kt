package io.aetera.usecase.goal

import io.aetera.model.goal.Goal
import io.aetera.model.goal.GoalPeriod
import java.time.LocalDate
import java.util.UUID

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
    constructor(goal: Goal) : this(
        id = goal.id.value,
        title = goal.title,
        period = goal.period,
        target = goal.target,
        unit = goal.unit,
        progress = goal.progress,
        periodStart = goal.periodStart,
        achieved = goal.isAchieved,
    )
}
