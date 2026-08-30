package io.aetera.controller.expense

import io.aetera.model.expense.ExpenseCategory
import io.aetera.model.expense.ExpenseCycle
import io.aetera.usecase.expense.cmd.SaveExpenseCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class ExpenseReq(
    @field:NotBlank
    @field:Size(max = 100)
    @field:Schema(example = "월세")
    val title: String,
    val category: ExpenseCategory,
    /** 한 주기에 내는 금액. 원 단위. 범위는 모델이 본다. */
    @field:Schema(example = "700000")
    val amount: Long,
    val cycle: ExpenseCycle,
    @field:Size(max = 500)
    val memo: String? = null,
) {
    fun toCommand(userId: UUID): SaveExpenseCommand = SaveExpenseCommand(
        userId = userId,
        title = title,
        category = category,
        amount = amount,
        cycle = cycle,
        memo = memo,
    )
}
