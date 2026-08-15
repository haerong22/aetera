package io.aetera.gateway.guide

import io.aetera.gateway.common.saveMerging
import io.aetera.model.guide.GuideJourneyId
import io.aetera.model.guide.GuideTaskKey
import io.aetera.model.guide.GuideTaskProgress
import io.aetera.model.guide.GuideTaskProgressRepository
import org.springframework.stereotype.Repository

@Repository
class GuideTaskProgressRepositoryJpaAdapter(
    private val guideTaskProgressJpaRepository: GuideTaskProgressJpaRepository,
) : GuideTaskProgressRepository {
    override fun save(progress: GuideTaskProgress): GuideTaskProgress = guideTaskProgressJpaRepository
        .saveMerging(
            id = progress.id.value,
            update = { it.applyFrom(progress) },
            create = { GuideTaskProgressJpaEntity.from(progress) },
        ).toModel()

    override fun findAllByJourneyId(journeyId: GuideJourneyId): List<GuideTaskProgress> = guideTaskProgressJpaRepository
        .findAllByJourneyId(journeyId.value)
        .map { it.toModel() }

    override fun getByJourneyIdAndTaskKey(
        journeyId: GuideJourneyId,
        taskKey: GuideTaskKey,
    ): GuideTaskProgress? = guideTaskProgressJpaRepository
        .findByJourneyIdAndTaskKey(journeyId.value, taskKey.value)
        ?.toModel()

    override fun delete(progress: GuideTaskProgress) {
        guideTaskProgressJpaRepository.deleteById(progress.id.value)
    }

    override fun deleteAllByJourneyId(journeyId: GuideJourneyId) {
        guideTaskProgressJpaRepository.deleteAllByJourneyId(journeyId.value)
    }
}
