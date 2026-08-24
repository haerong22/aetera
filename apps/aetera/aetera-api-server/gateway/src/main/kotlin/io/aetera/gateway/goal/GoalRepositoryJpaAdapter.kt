package io.aetera.gateway.goal

import io.aetera.gateway.common.saveMerging
import io.aetera.model.goal.Goal
import io.aetera.model.goal.GoalId
import io.aetera.model.goal.GoalRepository
import io.aetera.model.user.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class GoalRepositoryJpaAdapter(
    private val goalJpaRepository: GoalJpaRepository,
) : GoalRepository {
    override fun save(goal: Goal): Goal = goalJpaRepository
        .saveMerging(
            id = goal.id.value,
            update = { it.applyFrom(goal) },
            create = { GoalJpaEntity.from(goal) },
        ).toModel()

    override fun getById(id: GoalId): Goal? = goalJpaRepository.findByIdOrNull(id.value)?.toModel()

    override fun findAllByUserId(userId: UserId): List<Goal> = goalJpaRepository
        .findAllByUserId(userId.value)
        .map { it.toModel() }

    override fun delete(goal: Goal) {
        goalJpaRepository.deleteById(goal.id.value)
    }
}
