package io.aetera.controller.spending

import io.aetera.usecase.spending.cmd.SaveSpendingCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class SpendingReq(
    /** 원 단위. 그 달에 쓴 돈 총액이다. */
    @field:Schema(example = "480000")
    val amount: Long,
    @field:Schema(example = "추석 선물")
    val note: String? = null,
) {
    fun toCommand(
        userId: UUID,
        month: LocalDate,
    ): SaveSpendingCommand = SaveSpendingCommand(
        userId = userId,
        month = month,
        amount = amount,
        note = note,
    )
}
