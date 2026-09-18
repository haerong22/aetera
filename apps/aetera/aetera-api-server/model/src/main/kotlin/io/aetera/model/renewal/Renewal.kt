package io.aetera.model.renewal

import io.aetera.model.common.UserOwned
import io.aetera.model.common.optionalText
import io.aetera.model.common.requiredText
import io.aetera.model.user.UserId
import io.aetera.shared.error.ensure
import java.time.Instant
import java.time.LocalDate

class Renewal private constructor(
    val id: RenewalId,
    override val userId: UserId,
    title: String,
    category: RenewalCategory,
    expiresAt: LocalDate,
    cycle: RenewalCycle,
    noticeDays: Int,
    memo: String?,
    val createdAt: Instant,
) : UserOwned {
    var title: String = title
        private set

    var category: RenewalCategory = category
        private set

    var expiresAt: LocalDate = expiresAt
        private set

    var cycle: RenewalCycle = cycle
        private set

    var noticeDays: Int = noticeDays
        private set

    var memo: String? = memo
        private set

    fun update(
        title: String,
        category: RenewalCategory,
        expiresAt: LocalDate,
        cycle: RenewalCycle,
        noticeDays: Int,
        memo: String?,
        today: LocalDate,
    ) {
        this.title = validateTitle(title)
        this.category = category
        this.expiresAt = validateExpiry(expiresAt, today)
        this.cycle = cycle
        this.noticeDays = validateNoticeDays(noticeDays)
        this.memo = validateMemo(memo)
    }

    /**
     * 갱신했다 — 다음 만기로 굴린다.
     *
     * 기준은 `max(기존 만기, 오늘)` 이다. 만기 전에 미리 갱신하면 보험·계약이 기존 만기부터
     * 이어지고, 늦게 갱신하면 갱신한 날부터 시작하기 때문이다. 둘 중 하나로만 계산하면
     * 한쪽이 매번 어긋난다.
     */
    fun renew(today: LocalDate) {
        expiresAt = nextExpiryFrom(today)
    }

    /**
     * 지금 알려야 하는가 — 만기가 [noticeDays] 안으로 들어왔거나 이미 지났는가.
     *
     * "언제부터 급한가"는 항목마다 다르다. 여권은 6개월 전, 보험은 한 달 전이다.
     *
     * 지난 것도 포함한다. 만기가 지났는데 아직 갱신하지 않았다면 그게 가장 급한 일이고,
     * 조용해지는 순간 놓친 것을 영영 모른다.
     *
     * **같은 규칙이 브라우저에도 있다**(`modules/renewal/labels.ts` 의 `renewalStatus`).
     * 합칠 수 없어서 둘이다 — 서버는 줄을 색칠할 수 없고 브라우저는 메일을 보낼 수 없다.
     * 한쪽을 고치면 다른 쪽도 고친다.
     */
    fun needsNotice(today: LocalDate): Boolean = !expiresAt.isAfter(today.plusDays(noticeDays.toLong()))

    /** 갱신하면 언제가 되는지. 화면이 미리 보여줄 수 있도록 계산만 따로 꺼내 둔다. */
    fun nextExpiryFrom(today: LocalDate): LocalDate {
        ensure(cycle.repeats, RenewalErrorCode.CYCLE_NOT_REPEATABLE, "주기가 없는 항목입니다. id=$id")
        return cycle.nextFrom(maxOf(expiresAt, today))
    }

    override fun equals(other: Any?): Boolean = this === other || (other is Renewal && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Renewal(id=$id, title=$title, expiresAt=$expiresAt)"

    companion object {
        private const val TITLE_MAX_LENGTH = 100
        private const val MEMO_MAX_LENGTH = 500
        private const val NOTICE_DAYS_MAX = 365
        private const val EXPIRY_RANGE_YEARS = 30L

        const val DEFAULT_NOTICE_DAYS: Int = 30

        fun create(
            id: RenewalId,
            userId: UserId,
            title: String,
            category: RenewalCategory,
            expiresAt: LocalDate,
            cycle: RenewalCycle,
            noticeDays: Int,
            memo: String?,
            today: LocalDate,
            createdAt: Instant,
        ): Renewal = Renewal(
            id = id,
            userId = userId,
            title = validateTitle(title),
            category = category,
            expiresAt = validateExpiry(expiresAt, today),
            cycle = cycle,
            noticeDays = validateNoticeDays(noticeDays),
            memo = validateMemo(memo),
            createdAt = createdAt,
        )

        fun reconstitute(
            id: RenewalId,
            userId: UserId,
            title: String,
            category: RenewalCategory,
            expiresAt: LocalDate,
            cycle: RenewalCycle,
            noticeDays: Int,
            memo: String?,
            createdAt: Instant,
        ): Renewal = Renewal(id, userId, title, category, expiresAt, cycle, noticeDays, memo, createdAt)

        private fun validateTitle(title: String): String = requiredText(title, TITLE_MAX_LENGTH, RenewalErrorCode.INVALID_TITLE, "이름")

        private fun validateExpiry(
            expiresAt: LocalDate,
            today: LocalDate,
        ): LocalDate {
            ensure(
                expiresAt.isAfter(today.minusYears(EXPIRY_RANGE_YEARS)) &&
                    expiresAt.isBefore(today.plusYears(EXPIRY_RANGE_YEARS)),
                RenewalErrorCode.INVALID_EXPIRY_DATE,
                "만기일은 오늘로부터 ${EXPIRY_RANGE_YEARS}년 이내여야 합니다. 입력: $expiresAt",
            )
            return expiresAt
        }

        private fun validateNoticeDays(noticeDays: Int): Int {
            ensure(
                noticeDays in 0..NOTICE_DAYS_MAX,
                RenewalErrorCode.INVALID_NOTICE_DAYS,
                "미리 알림은 0~${NOTICE_DAYS_MAX}일 사이여야 합니다. 입력: $noticeDays",
            )
            return noticeDays
        }

        private fun validateMemo(memo: String?): String? = optionalText(memo, MEMO_MAX_LENGTH, RenewalErrorCode.MEMO_TOO_LONG, "메모")
    }
}
