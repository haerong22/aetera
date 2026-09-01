package io.aetera.model.asset

import io.aetera.model.common.UserOwned
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.aetera.shared.error.ensure
import java.time.Instant
import java.time.LocalDate

/**
 * 어느 달의 자산 한 줄. "2026년 9월, 주거래 통장, 현금, 1,200만원".
 *
 * **한 번 적으면 고치지 않는다.** 스냅샷은 그때의 사실이라, 이름을 바꾸면 지난 달의 기록까지
 * 소급해 바뀐다. 통장 이름을 바꾸고 싶으면 이번 달부터 새 이름으로 적으면 된다.
 * 그래서 이 클래스에는 `update` 가 없다 — 한 달을 고치는 일은 그 달을 통째로 다시 쓰는 것이다.
 *
 * 고정지출과 축이 다르다: 저쪽은 "매달 얼마 나가나"이고 여기는 "지금 얼마 갖고 있나"다.
 */
class AssetEntry private constructor(
    val id: AssetEntryId,
    override val userId: UserId,
    /** 이 기록이 속한 달. 언제나 그 달의 1일이다. */
    val month: LocalDate,
    val name: String,
    val category: AssetCategory,
    val amount: Long,
    val recordedAt: Instant,
) : UserOwned {
    /** 순자산에 더해질 부호 있는 금액. 부채는 빼는 쪽이다. */
    val signedAmount: Long get() = if (category.liability) -amount else amount

    override fun equals(other: Any?): Boolean = this === other || (other is AssetEntry && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "AssetEntry(month=$month, name=$name, amount=$amount)"

    companion object {
        private const val NAME_MAX_LENGTH = 100
        private const val AMOUNT_MAX = 1_000_000_000_000L
        private const val MONTH_RANGE_YEARS = 30L

        /** 한 달에 담을 수 있는 줄 수. 계좌를 이보다 많이 적는 사람은 이 도구가 필요한 사람이 아니다. */
        const val MAX_ENTRIES_PER_MONTH: Int = 50

        fun create(
            id: AssetEntryId,
            userId: UserId,
            month: LocalDate,
            name: String,
            category: AssetCategory,
            amount: Long,
            today: LocalDate,
            recordedAt: Instant,
        ): AssetEntry = AssetEntry(
            id = id,
            userId = userId,
            month = validateMonth(month, today),
            name = validateName(name),
            category = category,
            amount = validateAmount(amount),
            recordedAt = recordedAt,
        )

        fun reconstitute(
            id: AssetEntryId,
            userId: UserId,
            month: LocalDate,
            name: String,
            category: AssetCategory,
            amount: Long,
            recordedAt: Instant,
        ): AssetEntry = AssetEntry(id, userId, month, name, category, amount, recordedAt)

        /** 달의 1일로 맞춘다. 화면이 며칠을 보내든 같은 달이면 같은 스냅샷이어야 한다. */
        fun normalizeMonth(month: LocalDate): LocalDate = month.withDayOfMonth(1)

        private fun validateMonth(
            month: LocalDate,
            today: LocalDate,
        ): LocalDate {
            val normalized = normalizeMonth(month)
            ensure(
                normalized.isAfter(today.minusYears(MONTH_RANGE_YEARS)) &&
                    normalized.isBefore(today.plusYears(MONTH_RANGE_YEARS)),
                AssetErrorCode.INVALID_MONTH,
                "기록할 달은 오늘로부터 ${MONTH_RANGE_YEARS}년 이내여야 합니다. 입력: $month",
            )
            return normalized
        }

        private fun validateName(name: String): String {
            val trimmed = name.trim()
            if (trimmed.isEmpty() || trimmed.length > NAME_MAX_LENGTH) {
                throw CoreException(
                    AssetErrorCode.INVALID_NAME,
                    "이름은 1자 이상 ${NAME_MAX_LENGTH}자 이하여야 합니다. 입력 길이: ${trimmed.length}",
                )
            }
            return trimmed
        }

        /**
         * 0 을 허용한다 — 잔액이 0 인 계좌도 "아직 갖고 있다"는 사실이다.
         * 없앤 계좌는 다음 달부터 안 적으면 된다.
         */
        private fun validateAmount(amount: Long): Long {
            ensure(
                amount in 0..AMOUNT_MAX,
                AssetErrorCode.INVALID_AMOUNT,
                "금액은 0원 이상 ${AMOUNT_MAX}원 이하여야 합니다. 입력: $amount",
            )
            return amount
        }
    }
}

/** 순자산. 가진 것에서 갚을 것을 뺀다. */
fun List<AssetEntry>.netWorth(): Long = sumOf { it.signedAmount }

/**
 * 당장 쓸 수 있는 돈.
 *
 * 투자·부동산·연금은 빼고 현금만 센다 — "몇 달 버티나"를 물을 때 집을 팔아서 버틴다고
 * 셈하지는 않기 때문이다.
 */
fun List<AssetEntry>.cashTotal(): Long = filter { it.category == AssetCategory.CASH }.sumOf { it.amount }
