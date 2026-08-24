package io.aetera.model.goal

import io.aetera.model.common.UserOwned
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.aetera.shared.error.ensure
import java.time.Instant
import java.time.LocalDate

/**
 * 주기마다 다시 재는 목표. "주 3회 운동", "월 100페이지 독서".
 *
 * 지난 주기의 성적을 기록하지 않는다 — 그건 타임라인(관측형 모듈)의 일이고,
 * 여기서 쌓아 두면 아무도 읽지 않는 표가 하나 생긴다. 필요해지면 그때 만든다.
 */
class Goal private constructor(
    val id: GoalId,
    override val userId: UserId,
    title: String,
    period: GoalPeriod,
    target: Int,
    unit: String?,
    progress: Int,
    periodStart: LocalDate,
    val createdAt: Instant,
) : UserOwned {
    var title: String = title
        private set

    var period: GoalPeriod = period
        private set

    var target: Int = target
        private set

    var unit: String? = unit
        private set

    /** 이 주기에 쌓인 양. [periodStart] 가 지나면 0 으로 돌아간다. */
    var progress: Int = progress
        private set

    var periodStart: LocalDate = periodStart
        private set

    val isAchieved: Boolean get() = progress >= target

    fun update(
        title: String,
        period: GoalPeriod,
        target: Int,
        unit: String?,
        today: LocalDate,
    ) {
        this.title = validateTitle(title)
        // 주기를 바꾸면 재는 창이 달라지므로 진행도를 이어받지 않는다.
        if (this.period != period) {
            this.period = period
            this.progress = 0
        }
        this.periodStart = this.period.startOf(today)
        this.target = validateTarget(target)
        this.unit = validateUnit(unit)
    }

    /**
     * 진행도를 [amount] 만큼 옮긴다. 음수면 되돌린다 — 잘못 눌렀을 때 취소할 수 있어야 한다.
     *
     * 주기가 넘어갔으면 먼저 리셋한다. 이 판단을 조회할 때가 아니라 기록할 때 하는 이유는,
     * 화면을 열어 두기만 해도 저장이 일어나는 걸 막기 위해서다.
     */
    fun addProgress(
        amount: Int,
        today: LocalDate,
    ) {
        rollOverIfNeeded(today)
        progress = (progress + amount).coerceAtLeast(0)
    }

    /** 주기가 지났으면 진행도를 0 으로 돌린다. 조회 시점의 표시를 맞추는 데도 쓴다. */
    fun rollOverIfNeeded(today: LocalDate) {
        val currentStart = period.startOf(today)
        if (currentStart.isAfter(periodStart)) {
            periodStart = currentStart
            progress = 0
        }
    }

    override fun equals(other: Any?): Boolean = this === other || (other is Goal && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Goal(id=$id, title=$title, progress=$progress/$target)"

    companion object {
        private const val TITLE_MAX_LENGTH = 100
        private const val UNIT_MAX_LENGTH = 10
        private const val TARGET_MAX = 100_000

        fun create(
            id: GoalId,
            userId: UserId,
            title: String,
            period: GoalPeriod,
            target: Int,
            unit: String?,
            today: LocalDate,
            createdAt: Instant,
        ): Goal = Goal(
            id = id,
            userId = userId,
            title = validateTitle(title),
            period = period,
            target = validateTarget(target),
            unit = validateUnit(unit),
            progress = 0,
            periodStart = period.startOf(today),
            createdAt = createdAt,
        )

        fun reconstitute(
            id: GoalId,
            userId: UserId,
            title: String,
            period: GoalPeriod,
            target: Int,
            unit: String?,
            progress: Int,
            periodStart: LocalDate,
            createdAt: Instant,
        ): Goal = Goal(id, userId, title, period, target, unit, progress, periodStart, createdAt)

        private fun validateTitle(title: String): String {
            val trimmed = title.trim()
            if (trimmed.isEmpty() || trimmed.length > TITLE_MAX_LENGTH) {
                throw CoreException(
                    GoalErrorCode.INVALID_TITLE,
                    "목표 이름은 1자 이상 ${TITLE_MAX_LENGTH}자 이하여야 합니다. 입력 길이: ${trimmed.length}",
                )
            }
            return trimmed
        }

        private fun validateTarget(target: Int): Int {
            ensure(
                target in 1..TARGET_MAX,
                GoalErrorCode.INVALID_TARGET,
                "목표치는 1 이상 ${TARGET_MAX} 이하여야 합니다. 입력: $target",
            )
            return target
        }

        private fun validateUnit(unit: String?): String? {
            val trimmed = unit?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            ensure(
                trimmed.length <= UNIT_MAX_LENGTH,
                GoalErrorCode.INVALID_UNIT,
                "단위는 ${UNIT_MAX_LENGTH}자 이하여야 합니다. 입력 길이: ${trimmed.length}",
            )
            return trimmed
        }
    }
}
