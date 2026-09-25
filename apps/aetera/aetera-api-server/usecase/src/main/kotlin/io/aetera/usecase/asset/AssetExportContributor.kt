package io.aetera.usecase.asset

import io.aetera.model.asset.AssetEntryRepository
import io.aetera.model.module.ExportContributor
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component

/** 달마다 찍은 자산 한 줄씩. 부호는 분류가 정하므로 `signedAmount` 도 함께 낸다. */
@Component
class AssetExportContributor(
    private val assetEntryRepository: AssetEntryRepository,
) : ExportContributor {
    override val section: String = AssetModule.MODULE_ID.value

    override fun exportFor(userId: UserId): List<Map<String, Any?>> = assetEntryRepository
        .findAllByUserId(userId)
        .map { entry ->
            mapOf(
                "month" to entry.month.toString(),
                "name" to entry.name,
                "category" to entry.category.name,
                "amount" to entry.amount,
                "signedAmount" to entry.signedAmount,
                "recordedAt" to entry.recordedAt.toString(),
            )
        }
}
