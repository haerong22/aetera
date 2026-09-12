package io.aetera.usecase.income

import io.aetera.model.income.IncomeErrorCode
import io.aetera.model.income.IncomeSourceId
import io.aetera.model.income.IncomeSourceRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.orNotFound
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteIncomeService(
    private val incomeSourceRepository: IncomeSourceRepository,
    private val findIncomesService: FindIncomesService,
) {
    @Transactional
    fun delete(
        userId: UUID,
        incomeId: UUID,
    ): IncomeBoardDto {
        val source =
            incomeSourceRepository
                .getById(IncomeSourceId(incomeId))
                .orNotFound(UserId(userId), IncomeErrorCode.INCOME_NOT_FOUND, incomeId)

        incomeSourceRepository.delete(source)
        return findIncomesService.findIncomes(userId)
    }
}
