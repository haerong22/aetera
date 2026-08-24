package io.aetera.usecase.goal

import io.aetera.model.goal.GoalErrorCode
import io.aetera.model.goal.GoalId
import io.aetera.model.goal.GoalRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.orNotFound
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteGoalService(
    private val goalRepository: GoalRepository,
) {
    @Transactional
    fun delete(
        userId: UUID,
        goalId: UUID,
    ) {
        val id = GoalId(goalId)
        goalRepository.delete(goalRepository.getById(id).orNotFound(UserId(userId), GoalErrorCode.GOAL_NOT_FOUND, id))
    }
}
