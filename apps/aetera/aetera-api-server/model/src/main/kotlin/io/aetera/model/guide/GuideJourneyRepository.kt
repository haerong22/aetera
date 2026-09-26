package io.aetera.model.guide

import io.aetera.model.user.UserId

interface GuideJourneyRepository {
    fun save(journey: GuideJourney): GuideJourney

    /** 타임라인이 쓴다 — 어느 가이드를 시작했는지 한 번에 알아야 한다. */
    fun findAllByUserId(userId: UserId): List<GuideJourney>

    fun getByUserIdAndGuideId(
        userId: UserId,
        guideId: GuideId,
    ): GuideJourney?

    /** 탈퇴할 때 한 번. 이 사용자의 것을 통째로 지운다. */
    fun deleteAllByUserId(userId: UserId)

    fun delete(journey: GuideJourney)
}
