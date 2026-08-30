package io.aetera.usecase.expense

import io.aetera.model.expense.ExpenseErrorCode
import io.aetera.model.expense.FixedExpenseId
import io.aetera.model.expense.FixedExpenseRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.orNotFound
import io.aetera.usecase.expense.cmd.SaveExpenseCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UpdateExpenseService(
    private val fixedExpenseRepository: FixedExpenseRepository,
    private val findExpensesService: FindExpensesService,
) {
    @Transactional
    fun update(
        expenseId: UUID,
        command: SaveExpenseCommand,
    ): ExpenseBoardDto {
        val expense =
            fixedExpenseRepository
                .getById(FixedExpenseId(expenseId))
                .orNotFound(UserId(command.userId), ExpenseErrorCode.EXPENSE_NOT_FOUND, expenseId)

        expense.update(command.title, command.category, command.amount, command.cycle, command.memo)
        fixedExpenseRepository.save(expense)
        return findExpensesService.findExpenses(command.userId)
    }
}
