package io.aetera.gateway.guide

import io.aetera.gateway.common.saveMerging
import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideJourney
import io.aetera.model.guide.GuideJourneyRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Repository

@Repository
class GuideJourneyRepositoryJpaAdapter(
    private val guideJourneyJpaRepository: GuideJourneyJpaRepository,
) : GuideJourneyRepository {
    override fun save(journey: GuideJourney): GuideJourney = guideJourneyJpaRepository
        .saveMerging(
            id = journey.id.value,
            update = { it.applyFrom(journey) },
            create = { GuideJourneyJpaEntity.from(journey) },
        ).toModel()

    override fun getByUserIdAndGuideId(
        userId: UserId,
        guideId: GuideId,
    ): GuideJourney? = guideJourneyJpaRepository.findByUserIdAndGuideId(userId.value, guideId.value)?.toModel()

    override fun delete(journey: GuideJourney) {
        guideJourneyJpaRepository.deleteById(journey.id.value)
    }
}
