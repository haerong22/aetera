package io.aetera.usecase.spending.cmd

import java.time.LocalDate
import java.util.UUID

data class SaveSpendingCommand(
    val userId: UUID,
    val month: LocalDate,
    val amount: Long,
    val note: String?,
)
