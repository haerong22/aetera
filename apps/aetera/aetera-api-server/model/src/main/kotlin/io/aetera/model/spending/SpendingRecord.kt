package io.aetera.model.spending

import io.aetera.model.common.UserOwned
import io.aetera.model.common.optionalText
import io.aetera.model.user.UserId
import io.aetera.shared.error.ensure
import java.time.Instant
import java.time.LocalDate

/**
 * 어느 달에 쓴 변동지출 총액. "2026년 9월, 48만원".
 *
 * ## 왜 총액 하나인가
 *
 * 영수증을 매번 적게 하는 도구는 2주면 안 쓴다. 그래서 자산 모듈과 같은 결로 간다 —
 * **달에 한 번, 숫자 하나.** 카드 명세서 합계를 보고 옮겨 적는 정도의 일이다.
 *
 * 분류를 두지 않는 것도 같은 이유다. 식비·교통비를 나누려면 영수증 단위로 적어야 하고,
 * 그건 이 모듈이 하려는 일이 아니다. 나누어 보고 싶은 사람에게는 가계부가 맞다.
 *
 * ## 고정지출과 무엇이 다른가
 *
 * 저쪽은 **약속된 돈**이다 — 월세처럼 다음 달에도 같은 금액이 나갈 것을 지금 안다.
 * 여기는 **지나 봐야 아는 돈**이다. 그래서 저쪽은 항목을 등록해 두고, 여기는 달이 끝난 뒤 적는다.
 *
 * 퇴사 준비의 런웨이가 둘을 더해 "한 달에 얼마 나가나"를 만든다. 지금까지 변동비 자리는
 * 사용자가 짐작해 적는 선택 칸이었고, 비워 두면 0으로 셈했다 — 그러면 답이 실제보다 넉넉하다.
 */
class SpendingRecord private constructor(
    val id: SpendingRecordId,
    override val userId: UserId,
    /** 이 기록이 속한 달. 언제나 그 달의 1일이다. */
    val month: LocalDate,
    val amount: Long,
    val note: String?,
    val recordedAt: Instant,
) : UserOwned {
    override fun equals(other: Any?): Boolean = this === other || (other is SpendingRecord && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "SpendingRecord(month=$month, amount=$amount)"

    companion object {
        private const val NOTE_MAX_LENGTH = 200
        private const val AMOUNT_MAX = 1_000_000_000_000L
        private const val MONTH_RANGE_YEARS = 30L

        fun create(
            id: SpendingRecordId,
            userId: UserId,
            month: LocalDate,
            amount: Long,
            note: String?,
            today: LocalDate,
            recordedAt: Instant,
        ): SpendingRecord = SpendingRecord(
            id = id,
            userId = userId,
            month = validateMonth(month, today),
            amount = validateAmount(amount),
            note = optionalText(note, NOTE_MAX_LENGTH, SpendingErrorCode.INVALID_NOTE, "메모"),
            recordedAt = recordedAt,
        )

        fun reconstitute(
            id: SpendingRecordId,
            userId: UserId,
            month: LocalDate,
            amount: Long,
            note: String?,
            recordedAt: Instant,
        ): SpendingRecord = SpendingRecord(id, userId, month, amount, note, recordedAt)

        /** 달의 1일로 맞춘다. 화면이 며칠을 보내든 같은 달이면 같은 기록이어야 한다. */
        fun normalizeMonth(month: LocalDate): LocalDate = month.withDayOfMonth(1)

        private fun validateMonth(
            month: LocalDate,
            today: LocalDate,
        ): LocalDate {
            val normalized = normalizeMonth(month)
            ensure(
                normalized.isAfter(today.minusYears(MONTH_RANGE_YEARS)) &&
                    !normalized.isAfter(normalizeMonth(today)),
                SpendingErrorCode.INVALID_MONTH,
                "기록할 달은 이번 달까지여야 합니다. 입력: $month",
            )
            return normalized
        }

        /**
         * 0 을 허용한다 — 한 푼도 안 쓴 달은 드물지만, 적어 둘 수 있어야 **안 적은 달과 구분**된다.
         * 그 구분이 평균을 가른다: 안 적은 달은 평균에서 빠지고, 0원이라 적은 달은 평균을 끌어내린다.
         */
        private fun validateAmount(amount: Long): Long {
            ensure(
                amount in 0..AMOUNT_MAX,
                SpendingErrorCode.INVALID_AMOUNT,
                "금액은 0원 이상 ${AMOUNT_MAX}원 이하여야 합니다. 입력: $amount",
            )
            return amount
        }
    }
}

/**
 * 최근 [months] 달의 평균. 적지 않은 달은 **세지 않는다**.
 *
 * 빠진 달을 0 으로 치면 평균이 주저앉아, 런웨이가 실제보다 길게 나온다 — 돈 이야기에서
 * 틀리면 안 되는 방향이다. 기록이 없으면 `null` 을 주고, 쓰는 쪽이 "아직 모른다"로 다룬다.
 *
 * 금액과 **센 달 수를 함께** 돌려준다([MonthlyAverage]) — 따로 세면 창을 두 곳에서
 * 계산하게 되고, 그러면 "6개월 평균"이라 적힌 값이 실제로는 세 달치일 수 있다.
 */
fun List<SpendingRecord>.averageMonthly(
    today: LocalDate,
    months: Int,
): MonthlyAverage? {
    val from = SpendingRecord.normalizeMonth(today).minusMonths((months - 1).toLong())
    val recent = filter { !it.month.isBefore(from) }
    if (recent.isEmpty()) return null
    return MonthlyAverage(recent.sumOf { it.amount } / recent.size, recent.size)
}
