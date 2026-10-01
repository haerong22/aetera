package io.aetera.usecase.spending

import io.aetera.model.spending.MonthlyAverage
import io.aetera.model.spending.SpendingComparison
import io.aetera.model.spending.SpendingRecord
import io.aetera.model.spending.averageMonthly
import java.time.LocalDate

/**
 * 한 달의 기록.
 *
 * 평소와 견준 결과를 **서버가 판단해서** 넣는다. 문턱값을 화면에도 두면 타임라인과 목록이
 * 갈릴 수 있다 — 소득의 `continuesAfterLeaving`, 자산의 `signedAmount` 와 같은 자리다.
 */
data class SpendingRecordDto(
    val month: LocalDate,
    val amount: Long,
    val note: String?,
    /** 평균보다 얼마나 많은가. 음수면 적게 쓴 달이다. 견줄 평균이 없으면 null. */
    val versusAverage: Long?,
    /** 그 차이를 어떻게 읽을지. 말과 색은 보여 주는 쪽이 정한다. */
    val comparison: SpendingComparison?,
) {
    companion object {
        fun of(
            record: SpendingRecord,
            average: MonthlyAverage?,
        ): SpendingRecordDto {
            // 기록이 하나뿐이면 그 달이 곧 평균이라 견줄 것이 없다.
            val difference = if (average == null || average.months < 2) null else record.amount - average.amount
            return SpendingRecordDto(
                month = record.month,
                amount = record.amount,
                note = record.note,
                versusAverage = difference,
                comparison = difference?.let(SpendingComparison::of),
            )
        }
    }
}

/**
 * 변동지출 화면 하나를 그리는 데 필요한 전부.
 *
 * `records` 가 비어 있으면 아직 한 번도 적지 않은 상태다 — 오류가 아니라 시작 전이다.
 */
data class SpendingBoardDto(
    /** 최근 달부터. */
    val records: List<SpendingRecordDto>,
    /**
     * 최근 [AVERAGE_MONTHS] 달의 평균. 기록이 없으면 null.
     *
     * 다른 모듈이 "한 달에 얼마 쓰나"를 물을 때 쓰는 값이다. **적지 않은 달은 세지 않는다** —
     * 0 으로 치면 평균이 주저앉아 런웨이가 실제보다 길게 나온다.
     */
    val monthlyAverage: Long?,
    /** 평균을 낸 달 수. 한두 달치 평균을 "평소"로 읽지 않도록 화면이 함께 보여 준다. */
    val averagedMonths: Int,
) {
    companion object {
        /**
         * 평균을 내는 기간.
         *
         * 석 달은 짧아 한 번의 여행에 휘둘리고, 열두 달은 길어 작년 씀씀이가 지금 답에 섞인다.
         * 여섯 달이면 계절 한 바퀴의 절반쯤이라 치우침이 덜하다.
         */
        const val AVERAGE_MONTHS: Int = 6

        /** 화면이 그리는 만큼만 내려보낸다. 몇 년을 적어도 응답이 무한정 길어지지 않는다. */
        private const val HISTORY_MONTHS = 24

        fun of(
            records: List<SpendingRecord>,
            today: LocalDate,
        ): SpendingBoardDto {
            val average = records.averageMonthly(today, AVERAGE_MONTHS)

            return SpendingBoardDto(
                records =
                    records
                        .sortedByDescending { it.month }
                        .take(HISTORY_MONTHS)
                        .map { SpendingRecordDto.of(it, average) },
                monthlyAverage = average?.amount,
                averagedMonths = average?.months ?: 0,
            )
        }
    }
}
