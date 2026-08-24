package io.aetera.gateway.goal

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.goal.Goal
import io.aetera.model.goal.GoalId
import io.aetera.model.goal.GoalPeriod
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "goals")
class GoalJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "title", nullable = false, length = 100)
    var title: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false, length = 20)
    var period: GoalPeriod,
    @Column(name = "target", nullable = false)
    var target: Int,
    @Column(name = "unit", length = 10)
    var unit: String?,
    @Column(name = "progress", nullable = false)
    var progress: Int,
    @Column(name = "period_start", nullable = false)
    var periodStart: LocalDate,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
) : UuidJpaEntity(uid) {
    fun applyFrom(goal: Goal) {
        title = goal.title
        period = goal.period
        target = goal.target
        unit = goal.unit
        progress = goal.progress
        periodStart = goal.periodStart
    }

    fun toModel(): Goal = Goal.reconstitute(
        id = GoalId(uid),
        userId = UserId(userId),
        title = title,
        period = period,
        target = target,
        unit = unit,
        progress = progress,
        periodStart = periodStart,
        createdAt = createdAt,
    )

    companion object {
        fun from(goal: Goal): GoalJpaEntity = GoalJpaEntity(
            uid = goal.id.value,
            userId = goal.userId.value,
            title = goal.title,
            period = goal.period,
            target = goal.target,
            unit = goal.unit,
            progress = goal.progress,
            periodStart = goal.periodStart,
            createdAt = goal.createdAt,
        )
    }
}
