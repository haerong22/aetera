package io.aetera.usecase.expense

import io.aetera.model.expense.ExpenseErrorCode
import io.aetera.model.expense.FixedExpenseId
import io.aetera.model.expense.FixedExpenseRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.orNotFound
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteExpenseService(
    private val fixedExpenseRepository: FixedExpenseRepository,
    private val findExpensesService: FindExpensesService,
) {
    @Transactional
    fun delete(
        userId: UUID,
        expenseId: UUID,
    ): ExpenseBoardDto {
        val expense =
            fixedExpenseRepository
                .getById(FixedExpenseId(expenseId))
                .orNotFound(UserId(userId), ExpenseErrorCode.EXPENSE_NOT_FOUND, expenseId)

        fixedExpenseRepository.delete(expense)
        return findExpensesService.findExpenses(userId)
    }
}
