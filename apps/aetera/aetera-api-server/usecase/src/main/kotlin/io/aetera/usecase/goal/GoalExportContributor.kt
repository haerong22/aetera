package io.aetera.usecase.goal

import io.aetera.model.goal.GoalRepository
import io.aetera.model.module.ExportContributor
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component

/**
 * 목표는 **지금 주기의 진행만** 들고 있다. 지난 주기 성적은 애초에 저장하지 않으므로
 * 내보낼 것도 없다 — 없는 것을 지어내지 않고, `periodStart` 로 어느 주기의 값인지만 밝힌다.
 */
@Component
class GoalExportContributor(
    private val goalRepository: GoalRepository,
) : ExportContributor {
    override val section: String = GoalModule.MODULE_ID.value

    override fun exportFor(userId: UserId): List<Map<String, Any?>> = goalRepository
        .findAllByUserId(userId)
        .map { goal ->
            mapOf(
                "title" to goal.title,
                "period" to goal.period.name,
                "target" to goal.target,
                "unit" to goal.unit,
                "progress" to goal.progress,
                "periodStart" to goal.periodStart.toString(),
                "createdAt" to goal.createdAt.toString(),
            )
        }
}
