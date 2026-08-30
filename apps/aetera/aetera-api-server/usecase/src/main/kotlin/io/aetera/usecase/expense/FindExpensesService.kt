package io.aetera.usecase.expense

import io.aetera.model.expense.FixedExpenseRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 고정지출 화면 조립. 조회 API 이자 **모든 변경 API 의 응답을 만드는 곳**이기도 하다 —
 * 항목이 하나 바뀌면 합계가 함께 움직이므로 바뀐 전체를 그대로 돌려준다.
 */
@Service
@Transactional(readOnly = true)
class FindExpensesService(
    private val fixedExpenseRepository: FixedExpenseRepository,
) {
    fun findExpenses(userId: UUID): ExpenseBoardDto = ExpenseBoardDto.of(fixedExpenseRepository.findAllByUserId(UserId(userId)))
}
