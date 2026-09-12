package io.aetera.usecase.income

import io.aetera.model.income.IncomeSourceRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 소득 화면 조립. 조회 API 이자 **모든 변경 API 의 응답을 만드는 곳**이기도 하다 —
 * 항목이 하나 바뀌면 합계가 함께 움직이므로 바뀐 전체를 그대로 돌려준다.
 */
@Service
@Transactional(readOnly = true)
class FindIncomesService(
    private val incomeSourceRepository: IncomeSourceRepository,
) {
    fun findIncomes(userId: UUID): IncomeBoardDto = IncomeBoardDto.of(incomeSourceRepository.findAllByUserId(UserId(userId)))
}
