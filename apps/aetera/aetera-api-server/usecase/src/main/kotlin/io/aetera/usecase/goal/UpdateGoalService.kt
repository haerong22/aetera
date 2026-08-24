package io.aetera.usecase.goal

import io.aetera.model.goal.GoalErrorCode
import io.aetera.model.goal.GoalId
import io.aetera.model.goal.GoalRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.orNotFound
import io.aetera.usecase.common.today
import io.aetera.usecase.goal.cmd.SaveGoalCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class UpdateGoalService(
    private val goalRepository: GoalRepository,
    private val clock: Clock,
) {
    @Transactional
    fun update(
        goalId: UUID,
        command: SaveGoalCommand,
    ): GoalDto {
        val id = GoalId(goalId)
        val goal = goalRepository.getById(id).orNotFound(UserId(command.userId), GoalErrorCode.GOAL_NOT_FOUND, id)
        goal.update(
            title = command.title,
            period = command.period,
            target = command.target,
            unit = command.unit,
            today = clock.today(),
        )
        return GoalDto(goalRepository.save(goal))
    }
}
