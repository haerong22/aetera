package io.aetera.usecase.goal

import io.aetera.model.goal.GoalRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.today
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindGoalsService(
    private val goalRepository: GoalRepository,
    private val clock: Clock,
) {
    /**
     * 주기가 지난 목표는 0 부터 다시 보여준다. 저장하지는 않는다 —
     * 화면을 열어 두기만 해도 쓰기가 일어나면 읽기 전용 조회가 아니게 된다.
     * 실제 리셋은 다음 기록 시점에 남는다.
     */
    fun findGoals(userId: UUID): List<GoalDto> {
        val today = clock.today()
        return goalRepository
            .findAllByUserId(UserId(userId))
            .onEach { it.rollOverIfNeeded(today) }
            .map(::GoalDto)
    }
}
