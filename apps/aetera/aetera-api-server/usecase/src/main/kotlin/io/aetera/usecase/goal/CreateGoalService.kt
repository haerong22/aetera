package io.aetera.usecase.goal

import io.aetera.model.goal.Goal
import io.aetera.model.goal.GoalId
import io.aetera.model.goal.GoalRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.today
import io.aetera.usecase.goal.cmd.SaveGoalCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CreateGoalService(
    private val goalRepository: GoalRepository,
    private val clock: Clock,
) {
    @Transactional
    fun create(command: SaveGoalCommand): GoalDto {
        val goal =
            Goal.create(
                id = GoalId.next(),
                userId = UserId(command.userId),
                title = command.title,
                period = command.period,
                target = command.target,
                unit = command.unit,
                today = clock.today(),
                createdAt = clock.instant(),
            )
        return GoalDto(goalRepository.save(goal))
    }
}
