package io.aetera.usecase.goal

import io.aetera.model.goal.GoalErrorCode
import io.aetera.model.goal.GoalId
import io.aetera.model.goal.GoalRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.orNotFound
import io.aetera.usecase.common.today
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class AddGoalProgressService(
    private val goalRepository: GoalRepository,
    private val clock: Clock,
) {
    /** 진행도를 옮긴다. 음수면 되돌린다 — 잘못 눌렀을 때 취소할 수 있어야 한다. */
    @Transactional
    fun addProgress(
        userId: UUID,
        goalId: UUID,
        amount: Int,
    ): GoalDto {
        val id = GoalId(goalId)
        val goal = goalRepository.getById(id).orNotFound(UserId(userId), GoalErrorCode.GOAL_NOT_FOUND, id)
        goal.addProgress(amount, clock.today())
        return GoalDto(goalRepository.save(goal))
    }
}
