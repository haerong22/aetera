package io.aetera.model.income

import io.aetera.model.common.UserOwned
import io.aetera.model.common.optionalText
import io.aetera.model.common.requiredText
import io.aetera.model.user.UserId
import io.aetera.shared.error.ensure
import java.time.Instant

/**
 * 주기적으로 들어오는 돈 하나. 월급, 부업, 월세 수입, 배당, 연금.
 *
 * 고정지출과 부호만 반대인 것처럼 보이지만 **한 표에 같이 담지 않는다.** 섞으면
 * "한 달 고정지출 250만원"이라는 숫자가 뜻을 잃는다 — 들어오는 것과 나가는 것은
 * 각각 온전한 답이어야 하고, 둘을 견주는 일은 그 답을 받아 보는 화면의 몫이다.
 *
 * **실수령액을 적는다.** 세전 금액을 적으면 "몇 달 버티나"가 그만큼 넉넉해진다 —
 * 통장에 찍히는 숫자가 실제로 쓸 수 있는 돈이다.
 *
 * 입금일은 담지 않는다. 그건 "언제"라서 일정의 몫이고, 여기는 "얼마"만 묻는다.
 */
class IncomeSource private constructor(
    val id: IncomeSourceId,
    override val userId: UserId,
    title: String,
    category: IncomeCategory,
    amount: Long,
    cycle: IncomeCycle,
    memo: String?,
    val createdAt: Instant,
) : UserOwned {
    var title: String = title
        private set

    var category: IncomeCategory = category
        private set

    /** 한 주기에 받는 실수령액. 원 단위. */
    var amount: Long = amount
        private set

    var cycle: IncomeCycle = cycle
        private set

    var memo: String? = memo
        private set

    /**
     * 연 환산 금액.
     *
     * 주기가 제각각인 항목을 견주려면 한 단위로 맞춰야 하는데, **월이 아니라 연으로 올린다.**
     * 월로 내리면 분기 30만원이 10만원이 되고 그 버려진 원이 항목마다 쌓여 합계가 어긋난다
     * ([monthlyTotal] 참고).
     */
    val yearlyAmount: Long get() = amount * 12 / cycle.months

    /** 일을 그만둬도 이어지는 돈인지. 판단은 [IncomeCategory] 가 갖고 있다. */
    val continuesAfterLeaving: Boolean get() = !category.stopsWhenLeaving

    fun update(
        title: String,
        category: IncomeCategory,
        amount: Long,
        cycle: IncomeCycle,
        memo: String?,
    ) {
        this.title = validateTitle(title)
        this.category = category
        this.amount = validateAmount(amount)
        this.cycle = cycle
        this.memo = validateMemo(memo)
    }

    override fun equals(other: Any?): Boolean = this === other || (other is IncomeSource && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "IncomeSource(id=$id, title=$title, amount=$amount, cycle=$cycle)"

    companion object {
        private const val TITLE_MAX_LENGTH = 100
        private const val MEMO_MAX_LENGTH = 500

        /** 사람이 0을 몇 개 더 붙였을 때 말이 되는 답을 내놓지 않기 위한 상한. */
        private const val AMOUNT_MAX = 1_000_000_000L

        fun create(
            id: IncomeSourceId,
            userId: UserId,
            title: String,
            category: IncomeCategory,
            amount: Long,
            cycle: IncomeCycle,
            memo: String?,
            createdAt: Instant,
        ): IncomeSource = IncomeSource(
            id = id,
            userId = userId,
            title = validateTitle(title),
            category = category,
            amount = validateAmount(amount),
            cycle = cycle,
            memo = validateMemo(memo),
            createdAt = createdAt,
        )

        fun reconstitute(
            id: IncomeSourceId,
            userId: UserId,
            title: String,
            category: IncomeCategory,
            amount: Long,
            cycle: IncomeCycle,
            memo: String?,
            createdAt: Instant,
        ): IncomeSource = IncomeSource(id, userId, title, category, amount, cycle, memo, createdAt)

        private fun validateTitle(title: String): String = requiredText(title, TITLE_MAX_LENGTH, IncomeErrorCode.INVALID_TITLE, "이름")

        private fun validateAmount(amount: Long): Long {
            ensure(
                amount in 1..AMOUNT_MAX,
                IncomeErrorCode.INVALID_AMOUNT,
                "금액은 1원 이상 ${AMOUNT_MAX}원 이하여야 합니다. 입력: $amount",
            )
            return amount
        }

        private fun validateMemo(memo: String?): String? = optionalText(memo, MEMO_MAX_LENGTH, IncomeErrorCode.MEMO_TOO_LONG, "메모")
    }
}

/**
 * 한 달에 얼마가 들어오는지.
 *
 * 연으로 올려 합친 **뒤에 한 번만** 12로 나눈다. 항목마다 월로 내려 더하면 버려진 원이
 * 항목 수만큼 쌓여, 화면의 항목별 금액을 손으로 더한 값과 합계가 달라진다.
 */
fun List<IncomeSource>.monthlyTotal(): Long = yearlyTotal() / 12

fun List<IncomeSource>.yearlyTotal(): Long = sumOf { it.yearlyAmount }

/**
 * 일을 그만둬도 이어지는 한 달 소득.
 *
 * "몇 달 버티나"를 묻는 쪽이 이 값을 쓴다. 전체 소득을 넘기면 퇴사하면 끊길 월급까지
 * 세어 버려, 답이 실제보다 한참 넉넉해지거나 아예 "바닥나지 않는다"고 말하게 된다.
 */
fun List<IncomeSource>.monthlyContinuing(): Long = filter { it.continuesAfterLeaving }.monthlyTotal()
