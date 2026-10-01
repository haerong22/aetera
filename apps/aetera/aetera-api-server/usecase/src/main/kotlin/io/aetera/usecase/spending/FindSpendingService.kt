package io.aetera.usecase.spending

import io.aetera.model.spending.SpendingRecordRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindSpendingService(
    private val spendingRecordRepository: SpendingRecordRepository,
) {
    /**
     * [today] 를 받는다 — 평균이 "최근 몇 달"을 보려면 오늘이 언제인지 알아야 하는데,
     * 서버의 오늘은 사용자의 오늘이 아니다. 시간대가 다르면 달이 하나 어긋난다.
     */
    fun findBoard(
        userId: UUID,
        today: LocalDate,
    ): SpendingBoardDto = SpendingBoardDto.of(
        spendingRecordRepository.findAllByUserId(UserId(userId)),
        today,
    )
}
