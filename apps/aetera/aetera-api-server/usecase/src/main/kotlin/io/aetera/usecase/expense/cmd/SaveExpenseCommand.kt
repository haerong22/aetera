package io.aetera.usecase.expense.cmd

import io.aetera.model.expense.ExpenseCategory
import io.aetera.model.expense.ExpenseCycle
import java.util.UUID

data class SaveExpenseCommand(
    val userId: UUID,
    val title: String,
    val category: ExpenseCategory,
    val amount: Long,
    val cycle: ExpenseCycle,
    val memo: String?,
)
