package io.aetera.usecase.spending

import io.aetera.model.spending.SpendingErrorCode
import io.aetera.model.spending.SpendingRecord
import io.aetera.model.spending.SpendingRecordRepository
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class DeleteSpendingService(
    private val spendingRecordRepository: SpendingRecordRepository,
) {
    /**
     * 그 달의 기록을 지운다.
     *
     * 없는 달을 지우라고 하면 404 다 — 조용히 넘기면 화면이 "지웠다"고 답하는데, 사실은
     * 다른 사람의 달을 지우려 했거나 이미 지워진 것이라 사용자가 알아야 한다.
     */
    @Transactional
    fun delete(
        userId: UUID,
        month: LocalDate,
    ) {
        val owner = UserId(userId)
        val normalized = SpendingRecord.normalizeMonth(month)
        spendingRecordRepository.findByUserIdAndMonth(owner, normalized)
            ?: throw CoreException(SpendingErrorCode.SPENDING_NOT_FOUND, "그 달의 기록이 없습니다. month=$normalized")

        spendingRecordRepository.deleteByUserIdAndMonth(owner, normalized)
    }
}
