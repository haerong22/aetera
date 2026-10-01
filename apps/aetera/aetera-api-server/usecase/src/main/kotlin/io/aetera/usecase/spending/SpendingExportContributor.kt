package io.aetera.usecase.spending

import io.aetera.model.module.UserDataContributor
import io.aetera.model.spending.SpendingRecordRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component

/** 달마다 적은 총액 한 줄씩. */
@Component
class SpendingExportContributor(
    private val spendingRecordRepository: SpendingRecordRepository,
) : UserDataContributor {
    override val section: String = SpendingModule.MODULE_ID.value

    override fun exportFor(userId: UserId): List<Map<String, Any?>> = spendingRecordRepository
        .findAllByUserId(userId)
        .map { record ->
            mapOf(
                "month" to record.month.toString(),
                "amount" to record.amount,
                "note" to record.note,
                "recordedAt" to record.recordedAt.toString(),
            )
        }

    override fun deleteAllFor(userId: UserId) {
        spendingRecordRepository.deleteAllByUserId(userId)
    }
}
