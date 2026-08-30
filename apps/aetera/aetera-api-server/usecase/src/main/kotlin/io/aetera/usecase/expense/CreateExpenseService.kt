package io.aetera.usecase.expense

import io.aetera.model.expense.FixedExpense
import io.aetera.model.expense.FixedExpenseId
import io.aetera.model.expense.FixedExpenseRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.expense.cmd.SaveExpenseCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CreateExpenseService(
    private val fixedExpenseRepository: FixedExpenseRepository,
    private val findExpensesService: FindExpensesService,
    private val clock: Clock,
) {
    @Transactional
    fun create(command: SaveExpenseCommand): ExpenseBoardDto {
        fixedExpenseRepository.save(
            FixedExpense.create(
                id = FixedExpenseId.next(),
                userId = UserId(command.userId),
                title = command.title,
                category = command.category,
                amount = command.amount,
                cycle = command.cycle,
                memo = command.memo,
                createdAt = clock.instant(),
            ),
        )
        return findExpensesService.findExpenses(command.userId)
    }
}
