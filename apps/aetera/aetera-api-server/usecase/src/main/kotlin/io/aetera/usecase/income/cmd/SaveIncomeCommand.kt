package io.aetera.usecase.income.cmd

import io.aetera.model.income.IncomeCategory
import io.aetera.model.income.IncomeCycle
import java.util.UUID

data class SaveIncomeCommand(
    val userId: UUID,
    val title: String,
    val category: IncomeCategory,
    val amount: Long,
    val cycle: IncomeCycle,
    val memo: String?,
)
