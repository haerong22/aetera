package io.aetera.usecase.expense

import io.aetera.model.expense.FixedExpenseRepository
import io.aetera.model.module.ExportContributor
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component

@Component
class ExpenseExportContributor(
    private val fixedExpenseRepository: FixedExpenseRepository,
) : ExportContributor {
    override val section: String = ExpenseModule.MODULE_ID.value

    override fun exportFor(userId: UserId): List<Map<String, Any?>> = fixedExpenseRepository
        .findAllByUserId(userId)
        .map { expense ->
            mapOf(
                "title" to expense.title,
                "category" to expense.category.name,
                "amount" to expense.amount,
                "cycle" to expense.cycle.name,
                "yearlyAmount" to expense.yearlyAmount,
                "memo" to expense.memo,
                "createdAt" to expense.createdAt.toString(),
            )
        }
}
