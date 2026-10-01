package io.aetera.usecase.spending

import io.aetera.model.spending.SpendingRecord
import io.aetera.model.spending.SpendingRecordId
import io.aetera.model.spending.SpendingRecordRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.spending.cmd.SaveSpendingCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate

/**
 * 한 달의 변동지출을 적는다. **같은 달을 다시 적으면 덮어쓴다.**
 *
 * 새 줄을 쌓지 않는 이유 — 한 달에 쓴 돈은 하나뿐이다. 명세서를 다시 보고 고치는 일이
 * 흔한데, 그때마다 줄이 늘면 합계와 평균이 전부 틀어진다. 표의 유니크 인덱스가 같은 뜻을 지킨다.
 *
 * 고칠 때 **식별자를 그대로 쓴다.** 새로 만들면 지운 자리에 새 줄이 들어가는 셈이라,
 * 내보낸 데이터를 들고 비교하는 사람에게는 같은 달이 두 번 바뀐 것처럼 보인다.
 */
@Service
class SaveSpendingService(
    private val spendingRecordRepository: SpendingRecordRepository,
    private val clock: Clock,
) {
    @Transactional
    fun save(
        command: SaveSpendingCommand,
        today: LocalDate,
    ): SpendingRecord {
        val owner = UserId(command.userId)
        val month = SpendingRecord.normalizeMonth(command.month)
        val existing = spendingRecordRepository.findByUserIdAndMonth(owner, month)

        return spendingRecordRepository.save(
            SpendingRecord.create(
                id = existing?.id ?: SpendingRecordId.next(),
                userId = owner,
                month = month,
                amount = command.amount,
                note = command.note,
                today = today,
                recordedAt = clock.instant(),
            ),
        )
    }
}
