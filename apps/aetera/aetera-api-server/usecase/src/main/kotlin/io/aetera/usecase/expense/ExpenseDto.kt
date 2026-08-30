package io.aetera.usecase.expense

import io.aetera.model.expense.ExpenseCategory
import io.aetera.model.expense.ExpenseCycle
import io.aetera.model.expense.FixedExpense
import io.aetera.model.expense.monthlyTotal
import io.aetera.model.expense.yearlyTotal
import java.time.Instant
import java.util.UUID

/** 고정지출 한 줄. */
data class ExpenseDto(
    val id: UUID,
    val title: String,
    val category: ExpenseCategory,
    val amount: Long,
    val cycle: ExpenseCycle,
    /** 주기가 달라도 견줄 수 있도록 서버가 환산해 준다 — 화면마다 다시 계산하면 규칙이 갈린다. */
    val yearlyAmount: Long,
    val memo: String?,
    val createdAt: Instant,
) {
    constructor(expense: FixedExpense) : this(
        id = expense.id.value,
        title = expense.title,
        category = expense.category,
        amount = expense.amount,
        cycle = expense.cycle,
        yearlyAmount = expense.yearlyAmount,
        memo = expense.memo,
        createdAt = expense.createdAt,
    )
}

/**
 * 고정지출 화면 하나를 그리는 데 필요한 전부.
 *
 * 변경 API 도 이걸 통째로 돌려준다 — 항목 하나만 주면 프론트가 합계를 다시 계산해야 하고,
 * 그 계산이 서버와 어긋나는 순간을 사용자가 본다(가이드 화면과 같은 이유).
 */
data class ExpenseBoardDto(
    val items: List<ExpenseDto>,
    val monthlyTotal: Long,
    val yearlyTotal: Long,
) {
    companion object {
        /** 부담이 큰 것부터 보여준다. 무엇부터 줄일지 정하는 화면이라 등록 순서는 쓸모가 없다. */
        fun of(expenses: List<FixedExpense>): ExpenseBoardDto = ExpenseBoardDto(
            items =
                expenses
                    .sortedWith(compareByDescending<FixedExpense> { it.yearlyAmount }.thenBy { it.title })
                    .map(::ExpenseDto),
            monthlyTotal = expenses.monthlyTotal(),
            yearlyTotal = expenses.yearlyTotal(),
        )
    }
}
