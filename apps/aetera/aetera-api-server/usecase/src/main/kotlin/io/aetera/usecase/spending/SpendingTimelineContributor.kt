package io.aetera.usecase.spending

import io.aetera.model.module.ModuleId
import io.aetera.model.module.TimelineContributor
import io.aetera.model.module.TimelineEntry
import io.aetera.model.spending.SpendingComparison
import io.aetera.model.spending.SpendingRecordRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.money
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 달마다 쓴 돈을 타임라인에 낸다.
 *
 * 금액만 적지 않는다 — "9월 48만원"은 그것만 보면 많은지 적은지 알 수 없다. 그 기간의
 * 평균과 견줘 **"평소보다 12만원 많음"** 까지 적어 줘야 지나온 길에서 뜻이 생긴다.
 *
 * 평균은 **보여 주는 기간 안에서만** 낸다. 전체 기간으로 내면 3년 전 씀씀이가 올해 비교에
 * 섞이고, 타임라인은 한 해씩 넘겨 보는 화면이라 보는 사람은 그 사실을 알 수 없다.
 */
@Component
class SpendingTimelineContributor(
    private val spendingRecordRepository: SpendingRecordRepository,
) : TimelineContributor {
    override val moduleIds: Set<ModuleId> = setOf(SpendingModule.MODULE_ID)

    override fun entriesIn(
        userId: UserId,
        from: LocalDate,
        to: LocalDate,
    ): List<TimelineEntry> {
        val records = spendingRecordRepository.findAllByUserIdAndMonthBetween(userId, from, to)
        // 견줄 상대가 하나뿐이면 "평소"라는 말이 성립하지 않는다.
        val average = if (records.size < 2) null else records.sumOf { it.amount } / records.size

        return records.map { record ->
            TimelineEntry(
                moduleId = SpendingModule.MODULE_ID,
                on = record.month,
                title = "쓴 돈 ${money(record.amount)}원",
                detail = average?.let { versus(record.amount, it) } ?: record.note,
            )
        }
    }

    /**
     * 평균과의 차이를 사람 말로.
     *
     * **어느 갈래인지는 [SpendingComparison] 이 정한다.** 문턱값을 여기 두면 목록 화면과
     * 갈려서, 같은 달을 두고 타임라인은 "많음"이라 하고 목록은 "비슷"이라 할 수 있다.
     * 여기서 짓는 것은 말뿐이다 — 타임라인은 지난 일을 적는 자리라 "많음"처럼 끝낸다.
     */
    private fun versus(
        amount: Long,
        average: Long,
    ): String {
        val diff = amount - average
        return when (SpendingComparison.of(diff)) {
            SpendingComparison.MORE -> "평소보다 ${money(diff)}원 많음"
            SpendingComparison.LESS -> "평소보다 ${money(-diff)}원 적음"
            SpendingComparison.SIMILAR -> "평소와 비슷"
        }
    }
}
