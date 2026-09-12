package io.aetera.usecase.income

import io.aetera.model.income.IncomeSource
import io.aetera.model.income.IncomeSourceId
import io.aetera.model.income.IncomeSourceRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.income.cmd.SaveIncomeCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CreateIncomeService(
    private val incomeSourceRepository: IncomeSourceRepository,
    private val findIncomesService: FindIncomesService,
    private val clock: Clock,
) {
    @Transactional
    fun create(command: SaveIncomeCommand): IncomeBoardDto {
        incomeSourceRepository.save(
            IncomeSource.create(
                id = IncomeSourceId.next(),
                userId = UserId(command.userId),
                title = command.title,
                category = command.category,
                amount = command.amount,
                cycle = command.cycle,
                memo = command.memo,
                createdAt = clock.instant(),
            ),
        )
        return findIncomesService.findIncomes(command.userId)
    }
}
