package io.aetera.usecase.renewal

import io.aetera.model.module.ExportContributor
import io.aetera.model.renewal.RenewalRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component

@Component
class RenewalExportContributor(
    private val renewalRepository: RenewalRepository,
) : ExportContributor {
    override val section: String = RenewalModule.MODULE_ID.value

    override fun exportFor(userId: UserId): List<Map<String, Any?>> = renewalRepository
        .findAllByUserId(userId)
        .map { renewal ->
            mapOf(
                "title" to renewal.title,
                "category" to renewal.category.name,
                "expiresAt" to renewal.expiresAt.toString(),
                "cycle" to renewal.cycle.name,
                "noticeDays" to renewal.noticeDays,
                "memo" to renewal.memo,
                "createdAt" to renewal.createdAt.toString(),
            )
        }
}
