package io.aetera.model.guide

import io.aetera.model.user.UserId

interface GuideJourneyRepository {
    fun save(journey: GuideJourney): GuideJourney

    fun getByUserIdAndGuideId(
        userId: UserId,
        guideId: GuideId,
    ): GuideJourney?

    fun delete(journey: GuideJourney)
}
