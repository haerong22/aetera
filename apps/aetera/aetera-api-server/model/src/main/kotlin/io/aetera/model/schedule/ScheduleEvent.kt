package io.aetera.model.schedule

import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.aetera.shared.error.ensure
import java.time.Instant

/**
 * 일정 하나. 시각은 전부 UTC 로 저장하고 사용자 타임존 변환은 화면이 한다.
 *
 * [userId] 는 평범한 값이다 — 모듈 데이터는 사용자 테이블에 FK 를 걸지 않는다.
 * 모듈을 통째로 떼어 내거나 데이터를 지울 때 코어와 얽히지 않게 하기 위한 규칙이다.
 */
class ScheduleEvent private constructor(
    val id: ScheduleEventId,
    val userId: UserId,
    title: String,
    description: String?,
    startsAt: Instant,
    endsAt: Instant,
    allDay: Boolean,
    color: String?,
    val createdAt: Instant,
) {
    var title: String = title
        private set

    var description: String? = description
        private set

    var startsAt: Instant = startsAt
        private set

    var endsAt: Instant = endsAt
        private set

    var allDay: Boolean = allDay
        private set

    var color: String? = color
        private set

    fun update(
        title: String,
        description: String?,
        startsAt: Instant,
        endsAt: Instant,
        allDay: Boolean,
        color: String?,
    ) {
        validatePeriod(startsAt, endsAt)
        this.title = validateTitle(title)
        this.description = description?.takeIf { it.isNotBlank() }
        this.startsAt = startsAt
        this.endsAt = endsAt
        this.allDay = allDay
        this.color = color?.let(::validateColor)
    }

    override fun equals(other: Any?): Boolean = this === other || (other is ScheduleEvent && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "ScheduleEvent(id=$id, title=$title, startsAt=$startsAt)"

    companion object {
        private const val TITLE_MAX_LENGTH = 200
        private val COLOR_PATTERN = Regex("^#[0-9a-fA-F]{6}$")

        fun create(
            id: ScheduleEventId,
            userId: UserId,
            title: String,
            description: String?,
            startsAt: Instant,
            endsAt: Instant,
            allDay: Boolean,
            color: String?,
            createdAt: Instant,
        ): ScheduleEvent {
            validatePeriod(startsAt, endsAt)
            return ScheduleEvent(
                id = id,
                userId = userId,
                title = validateTitle(title),
                description = description?.takeIf { it.isNotBlank() },
                startsAt = startsAt,
                endsAt = endsAt,
                allDay = allDay,
                color = color?.let(::validateColor),
                createdAt = createdAt,
            )
        }

        fun reconstitute(
            id: ScheduleEventId,
            userId: UserId,
            title: String,
            description: String?,
            startsAt: Instant,
            endsAt: Instant,
            allDay: Boolean,
            color: String?,
            createdAt: Instant,
        ): ScheduleEvent = ScheduleEvent(id, userId, title, description, startsAt, endsAt, allDay, color, createdAt)

        private fun validateTitle(title: String): String {
            val trimmed = title.trim()
            if (trimmed.isEmpty() || trimmed.length > TITLE_MAX_LENGTH) {
                throw CoreException(
                    ScheduleErrorCode.INVALID_EVENT_TITLE,
                    "일정 제목은 1자 이상 ${TITLE_MAX_LENGTH}자 이하여야 합니다. 입력 길이: ${trimmed.length}",
                )
            }
            return trimmed
        }

        private fun validatePeriod(
            startsAt: Instant,
            endsAt: Instant,
        ) {
            ensure(
                !endsAt.isBefore(startsAt),
                ScheduleErrorCode.INVALID_EVENT_PERIOD,
                "종료($endsAt)가 시작($startsAt)보다 빠릅니다.",
            )
        }

        private fun validateColor(color: String): String {
            ensure(COLOR_PATTERN.matches(color), ScheduleErrorCode.INVALID_EVENT_COLOR, "입력값: $color")
            return color.lowercase()
        }
    }
}
