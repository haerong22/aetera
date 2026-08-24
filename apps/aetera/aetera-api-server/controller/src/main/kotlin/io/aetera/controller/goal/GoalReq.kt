package io.aetera.controller.goal

import io.aetera.model.goal.GoalPeriod
import io.aetera.usecase.goal.cmd.SaveGoalCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class GoalReq(
    @field:NotBlank
    @field:Size(max = 100)
    @field:Schema(example = "운동하기")
    val title: String,
    val period: GoalPeriod = GoalPeriod.WEEKLY,
    @field:Min(1)
    @field:Max(100_000)
    @field:Schema(example = "3")
    val target: Int,
    @field:Size(max = 10)
    @field:Schema(example = "회")
    val unit: String? = null,
) {
    fun toCommand(userId: UUID): SaveGoalCommand = SaveGoalCommand(
        userId = userId,
        title = title,
        period = period,
        target = target,
        unit = unit,
    )
}
