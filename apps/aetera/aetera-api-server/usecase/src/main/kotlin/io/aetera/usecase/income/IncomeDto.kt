package io.aetera.usecase.income

import io.aetera.model.income.IncomeCategory
import io.aetera.model.income.IncomeCycle
import io.aetera.model.income.IncomeSource
import io.aetera.model.income.monthlyContinuing
import io.aetera.model.income.monthlyTotal
import io.aetera.model.income.yearlyTotal
import java.time.Instant
import java.util.UUID

/** 소득 한 줄. */
data class IncomeDto(
    val id: UUID,
    val title: String,
    val category: IncomeCategory,
    val amount: Long,
    val cycle: IncomeCycle,
    /** 주기가 달라도 견줄 수 있도록 서버가 환산해 준다 — 화면마다 다시 계산하면 규칙이 갈린다. */
    val yearlyAmount: Long,
    /** 일을 그만둬도 이어지는 돈인지. 분류에서 나오는 값이지만, 화면이 규칙을 또 갖지 않도록 서버가 말해 준다. */
    val continuesAfterLeaving: Boolean,
    val memo: String?,
    val createdAt: Instant,
) {
    constructor(source: IncomeSource) : this(
        id = source.id.value,
        title = source.title,
        category = source.category,
        amount = source.amount,
        cycle = source.cycle,
        yearlyAmount = source.yearlyAmount,
        continuesAfterLeaving = source.continuesAfterLeaving,
        memo = source.memo,
        createdAt = source.createdAt,
    )
}

/**
 * 소득 화면 하나를 그리는 데 필요한 전부.
 *
 * 변경 API 도 이걸 통째로 돌려준다 — 항목 하나만 주면 프론트가 합계를 다시 계산해야 하고,
 * 그 계산이 서버와 어긋나는 순간을 사용자가 본다(고정지출과 같은 이유).
 */
data class IncomeBoardDto(
    val items: List<IncomeDto>,
    val monthlyTotal: Long,
    val yearlyTotal: Long,
    /** 월급처럼 그만두면 멈추는 것을 뺀 한 달 소득. "몇 달 버티나"를 묻는 쪽이 이 값을 쓴다. */
    val monthlyContinuing: Long,
) {
    companion object {
        /** 큰 것부터 보여준다. 어디서 얼마가 들어오는지 보는 화면이라 등록 순서는 쓸모가 없다. */
        fun of(sources: List<IncomeSource>): IncomeBoardDto = IncomeBoardDto(
            items =
                sources
                    .sortedWith(compareByDescending<IncomeSource> { it.yearlyAmount }.thenBy { it.title })
                    .map(::IncomeDto),
            monthlyTotal = sources.monthlyTotal(),
            yearlyTotal = sources.yearlyTotal(),
            monthlyContinuing = sources.monthlyContinuing(),
        )
    }
}
