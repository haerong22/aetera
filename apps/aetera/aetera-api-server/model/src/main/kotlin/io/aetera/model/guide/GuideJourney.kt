package io.aetera.model.guide

import io.aetera.model.user.UserId
import io.aetera.shared.error.ensure
import java.time.Instant
import java.time.LocalDate

/**
 * 사용자 한 명이 가이드 하나를 실제로 밟고 있는 상태. `(userId, guideId)` 당 최대 한 행.
 *
 * 여정이 갖는 개인 정보는 [anchorDate] 하나뿐이다 — 이 날짜가 콘텐츠의 상대 일수를
 * 내 달력의 실제 마감일로 바꾼다. 체크 상태는 [GuideTaskProgress] 가 따로 들고 있다.
 *
 * "완료" 상태를 두지 않는다. 완료는 필수 항목을 다 체크했는지에서 파생되므로
 * 따로 저장하면 두 값이 어긋날 수 있고, 어긋나면 저장된 쪽이 이긴다.
 *
 * [userId] 는 평범한 값이다 — 모듈 데이터는 사용자 테이블에 FK 를 걸지 않는다.
 */
class GuideJourney private constructor(
    val id: GuideJourneyId,
    val userId: UserId,
    val guideId: GuideId,
    anchorDate: LocalDate,
    val startedAt: Instant,
) {
    var anchorDate: LocalDate = anchorDate
        private set

    /** 기준일을 바꾸면 모든 마감일이 함께 움직인다 — 퇴사일이 밀리는 건 흔한 일이다. */
    fun changeAnchorDate(
        anchorDate: LocalDate,
        today: LocalDate,
    ) {
        this.anchorDate = validateAnchorDate(anchorDate, today)
    }

    override fun equals(other: Any?): Boolean = this === other || (other is GuideJourney && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "GuideJourney(userId=$userId, guideId=$guideId, anchorDate=$anchorDate)"

    companion object {
        /**
         * 기준일이 오늘에서 벗어날 수 있는 최대 연수. 오타로 `2206-09-30` 을 넣으면 모든 마감이
         * 180년 뒤로 가서 화면이 조용히 무의미해지므로 상식선에서 막는다.
         */
        private const val ANCHOR_RANGE_YEARS = 5L

        fun start(
            id: GuideJourneyId,
            userId: UserId,
            guideId: GuideId,
            anchorDate: LocalDate,
            today: LocalDate,
            now: Instant,
        ): GuideJourney = GuideJourney(
            id = id,
            userId = userId,
            guideId = guideId,
            anchorDate = validateAnchorDate(anchorDate, today),
            startedAt = now,
        )

        fun reconstitute(
            id: GuideJourneyId,
            userId: UserId,
            guideId: GuideId,
            anchorDate: LocalDate,
            startedAt: Instant,
        ): GuideJourney = GuideJourney(id, userId, guideId, anchorDate, startedAt)

        private fun validateAnchorDate(
            anchorDate: LocalDate,
            today: LocalDate,
        ): LocalDate {
            ensure(
                anchorDate.isAfter(today.minusYears(ANCHOR_RANGE_YEARS)) &&
                    anchorDate.isBefore(today.plusYears(ANCHOR_RANGE_YEARS)),
                GuideErrorCode.INVALID_ANCHOR_DATE,
                "기준일은 오늘로부터 ${ANCHOR_RANGE_YEARS}년 이내여야 합니다. 입력: $anchorDate",
            )
            return anchorDate
        }
    }
}
