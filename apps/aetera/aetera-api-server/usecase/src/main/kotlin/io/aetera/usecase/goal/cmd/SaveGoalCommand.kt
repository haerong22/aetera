package io.aetera.usecase.goal.cmd

import io.aetera.model.goal.GoalPeriod
import java.util.UUID

data class SaveGoalCommand(
    val userId: UUID,
    val title: String,
    val period: GoalPeriod,
    val target: Int,
    val unit: String?,
)
