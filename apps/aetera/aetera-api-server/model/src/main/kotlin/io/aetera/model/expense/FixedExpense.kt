package io.aetera.model.expense

import io.aetera.model.common.UserOwned
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.aetera.shared.error.ensure
import java.time.Instant

/**
 * 매달·매년 같은 주기로 빠져나가는 돈 하나. 월세, 통신비, 보험료, 구독료.
 *
 * 만기 관리와 축이 다르다 — 저쪽은 "언제 끝나나"(`expiresAt`)를 묻고 여기는 "얼마씩 나가나"를 묻는다.
 * 넷플릭스는 양쪽에 다 있을 수 있고, 두 화면이 답하는 질문이 다르다.
 *
 * 결제일은 담지 않는다. 그건 "언제"라서 만기 관리나 일정의 몫이고,
 * 여기서까지 물으면 같은 값을 두 군데 적게 된다.
 */
class FixedExpense private constructor(
    val id: FixedExpenseId,
    override val userId: UserId,
    title: String,
    category: ExpenseCategory,
    amount: Long,
    cycle: ExpenseCycle,
    memo: String?,
    val createdAt: Instant,
) : UserOwned {
    var title: String = title
        private set

    var category: ExpenseCategory = category
        private set

    /** 한 주기에 내는 금액. 원 단위. */
    var amount: Long = amount
        private set

    var cycle: ExpenseCycle = cycle
        private set

    var memo: String? = memo
        private set

    /**
     * 연 환산 금액.
     *
     * 주기가 제각각인 항목을 견주려면 한 단위로 맞춰야 하는데, **월이 아니라 연으로 올린다.**
     * 월로 내리면 분기 10만원이 33,333원이 되고 그 버려진 1원이 항목마다 쌓여 합계가 어긋난다
     * ([monthlyTotal] 참고).
     */
    val yearlyAmount: Long get() = amount * 12 / cycle.months

    fun update(
        title: String,
        category: ExpenseCategory,
        amount: Long,
        cycle: ExpenseCycle,
        memo: String?,
    ) {
        this.title = validateTitle(title)
        this.category = category
        this.amount = validateAmount(amount)
        this.cycle = cycle
        this.memo = validateMemo(memo)
    }

    override fun equals(other: Any?): Boolean = this === other || (other is FixedExpense && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "FixedExpense(id=$id, title=$title, amount=$amount, cycle=$cycle)"

    companion object {
        private const val TITLE_MAX_LENGTH = 100
        private const val MEMO_MAX_LENGTH = 500

        /** 사람이 0을 몇 개 더 붙였을 때 말이 되는 답을 내놓지 않기 위한 상한. */
        private const val AMOUNT_MAX = 1_000_000_000L

        fun create(
            id: FixedExpenseId,
            userId: UserId,
            title: String,
            category: ExpenseCategory,
            amount: Long,
            cycle: ExpenseCycle,
            memo: String?,
            createdAt: Instant,
        ): FixedExpense = FixedExpense(
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
            id: FixedExpenseId,
            userId: UserId,
            title: String,
            category: ExpenseCategory,
            amount: Long,
            cycle: ExpenseCycle,
            memo: String?,
            createdAt: Instant,
        ): FixedExpense = FixedExpense(id, userId, title, category, amount, cycle, memo, createdAt)

        private fun validateTitle(title: String): String {
            val trimmed = title.trim()
            if (trimmed.isEmpty() || trimmed.length > TITLE_MAX_LENGTH) {
                throw CoreException(
                    ExpenseErrorCode.INVALID_TITLE,
                    "이름은 1자 이상 ${TITLE_MAX_LENGTH}자 이하여야 합니다. 입력 길이: ${trimmed.length}",
                )
            }
            return trimmed
        }

        private fun validateAmount(amount: Long): Long {
            ensure(
                amount in 1..AMOUNT_MAX,
                ExpenseErrorCode.INVALID_AMOUNT,
                "금액은 1원 이상 ${AMOUNT_MAX}원 이하여야 합니다. 입력: $amount",
            )
            return amount
        }

        private fun validateMemo(memo: String?): String? {
            val trimmed = memo?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            if (trimmed.length > MEMO_MAX_LENGTH) {
                throw CoreException(
                    ExpenseErrorCode.MEMO_TOO_LONG,
                    "메모는 ${MEMO_MAX_LENGTH}자 이하여야 합니다. 입력 길이: ${trimmed.length}",
                )
            }
            return trimmed
        }
    }
}

/**
 * 한 달에 얼마가 빠져나가는지.
 *
 * 연으로 올려 합친 **뒤에 한 번만** 12로 나눈다. 항목마다 월로 내려 더하면 버려진 원이
 * 항목 수만큼 쌓여, 화면의 항목별 금액을 손으로 더한 값과 합계가 달라진다.
 */
fun List<FixedExpense>.monthlyTotal(): Long = yearlyTotal() / 12

fun List<FixedExpense>.yearlyTotal(): Long = sumOf { it.yearlyAmount }
