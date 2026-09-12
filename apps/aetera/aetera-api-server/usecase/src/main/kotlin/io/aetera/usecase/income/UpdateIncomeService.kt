package io.aetera.usecase.income

import io.aetera.model.income.IncomeErrorCode
import io.aetera.model.income.IncomeSourceId
import io.aetera.model.income.IncomeSourceRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.orNotFound
import io.aetera.usecase.income.cmd.SaveIncomeCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UpdateIncomeService(
    private val incomeSourceRepository: IncomeSourceRepository,
    private val findIncomesService: FindIncomesService,
) {
    @Transactional
    fun update(
        incomeId: UUID,
        command: SaveIncomeCommand,
    ): IncomeBoardDto {
        val source =
            incomeSourceRepository
                .getById(IncomeSourceId(incomeId))
                .orNotFound(UserId(command.userId), IncomeErrorCode.INCOME_NOT_FOUND, incomeId)

        source.update(command.title, command.category, command.amount, command.cycle, command.memo)
        incomeSourceRepository.save(source)
        return findIncomesService.findIncomes(command.userId)
    }
}
