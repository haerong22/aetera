package io.aetera.gateway.guide

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideJourney
import io.aetera.model.guide.GuideJourneyId
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "guide_journeys")
class GuideJourneyJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "guide_id", nullable = false, length = 50, updatable = false)
    val guideId: String,
    @Column(name = "anchor_date", nullable = false)
    var anchorDate: LocalDate,
    @Column(name = "started_at", nullable = false, updatable = false)
    val startedAt: Instant,
) : UuidJpaEntity(uid) {
    fun applyFrom(journey: GuideJourney) {
        anchorDate = journey.anchorDate
    }

    fun toModel(): GuideJourney = GuideJourney.reconstitute(
        id = GuideJourneyId(uid),
        userId = UserId(userId),
        guideId = GuideId(guideId),
        anchorDate = anchorDate,
        startedAt = startedAt,
    )

    companion object {
        fun from(journey: GuideJourney): GuideJourneyJpaEntity = GuideJourneyJpaEntity(
            uid = journey.id.value,
            userId = journey.userId.value,
            guideId = journey.guideId.value,
            anchorDate = journey.anchorDate,
            startedAt = journey.startedAt,
        )
    }
}
