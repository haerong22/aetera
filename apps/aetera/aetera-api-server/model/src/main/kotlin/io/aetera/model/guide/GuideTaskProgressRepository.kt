package io.aetera.model.guide

interface GuideTaskProgressRepository {
    fun save(progress: GuideTaskProgress): GuideTaskProgress

    /** 화면 하나가 이 호출 한 번으로 그려진다 — 항목마다 조회하지 않는다. */
    fun findAllByJourneyId(journeyId: GuideJourneyId): List<GuideTaskProgress>

    fun getByJourneyIdAndTaskKey(
        journeyId: GuideJourneyId,
        taskKey: GuideTaskKey,
    ): GuideTaskProgress?

    fun delete(progress: GuideTaskProgress)

    /** 여정을 초기화할 때. 여정보다 먼저 지워야 참조가 남지 않는다. */
    fun deleteAllByJourneyId(journeyId: GuideJourneyId)
}
