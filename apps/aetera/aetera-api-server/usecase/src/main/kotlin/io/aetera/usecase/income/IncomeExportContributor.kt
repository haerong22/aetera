package io.aetera.usecase.income

import io.aetera.model.income.IncomeSourceRepository
import io.aetera.model.module.ExportContributor
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component

@Component
class IncomeExportContributor(
    private val incomeSourceRepository: IncomeSourceRepository,
) : ExportContributor {
    override val section: String = IncomeModule.MODULE_ID.value

    override fun exportFor(userId: UserId): List<Map<String, Any?>> = incomeSourceRepository
        .findAllByUserId(userId)
        .map { source ->
            mapOf(
                "title" to source.title,
                "category" to source.category.name,
                "amount" to source.amount,
                "cycle" to source.cycle.name,
                // 주기가 제각각이라 연 환산을 함께 내야 다른 곳에서 견줄 수 있다.
                "yearlyAmount" to source.yearlyAmount,
                "continuesAfterLeaving" to source.continuesAfterLeaving,
                "memo" to source.memo,
                "createdAt" to source.createdAt.toString(),
            )
        }
}
