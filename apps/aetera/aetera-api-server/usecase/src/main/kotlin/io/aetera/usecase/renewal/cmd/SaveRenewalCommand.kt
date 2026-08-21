package io.aetera.usecase.renewal.cmd

import io.aetera.model.renewal.RenewalCategory
import io.aetera.model.renewal.RenewalCycle
import java.time.LocalDate
import java.util.UUID

data class SaveRenewalCommand(
    val userId: UUID,
    val title: String,
    val category: RenewalCategory,
    val expiresAt: LocalDate,
    val cycle: RenewalCycle,
    val noticeDays: Int,
    val memo: String?,
)
