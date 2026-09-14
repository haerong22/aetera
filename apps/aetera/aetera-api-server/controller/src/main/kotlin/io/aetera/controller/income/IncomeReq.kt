package io.aetera.controller.income

import io.aetera.model.income.IncomeCategory
import io.aetera.model.income.IncomeCycle
import io.aetera.usecase.income.cmd.SaveIncomeCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class IncomeReq(
    @field:Schema(example = "월급")
    val title: String,
    val category: IncomeCategory,
    /** 한 주기에 받는 실수령액. 원 단위. 범위는 모델이 본다. */
    @field:Schema(example = "3200000")
    val amount: Long,
    val cycle: IncomeCycle,
    val memo: String? = null,
) {
    fun toCommand(userId: UUID): SaveIncomeCommand = SaveIncomeCommand(
        userId = userId,
        title = title,
        category = category,
        amount = amount,
        cycle = cycle,
        memo = memo,
    )
}
