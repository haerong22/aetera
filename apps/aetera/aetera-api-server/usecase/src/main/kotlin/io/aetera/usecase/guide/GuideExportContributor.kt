package io.aetera.usecase.guide

import io.aetera.model.guide.GuideJourney
import io.aetera.model.guide.GuideJourneyRepository
import io.aetera.model.guide.GuideTaskProgressRepository
import io.aetera.model.module.ExportContributor
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component

/**
 * 시작한 가이드와 체크해 둔 할 일.
 *
 * 네 가이드를 한 덩어리로 낸다 — 읽는 방식이 같고, 받는 사람에게도 "내가 밟은 가이드들"이
 * 한 자리에 있는 편이 낫다. 어느 가이드인지는 줄마다 `guideId` 가 말한다.
 *
 * **체크한 것만 낸다.** 안 한 일은 콘텐츠를 보면 알 수 있고, 27개짜리 가이드 넷을 통째로
 * 실으면 내 기록보다 남의 양식이 더 많은 파일이 된다.
 */
@Component
class GuideExportContributor(
    private val guideJourneyRepository: GuideJourneyRepository,
    private val guideTaskProgressRepository: GuideTaskProgressRepository,
    private val guideCatalog: GuideCatalog,
) : ExportContributor {
    override val section: String = "guide"

    override fun exportFor(userId: UserId): List<Map<String, Any?>> = guideJourneyRepository
        .findAllByUserId(userId)
        .map { journey -> journeyOf(journey) }

    private fun journeyOf(journey: GuideJourney): Map<String, Any?> {
        val template = guideCatalog.findOrNull(journey.guideId)
        val titleByKey =
            template
                ?.phases
                ?.flatMap { it.tasks }
                ?.associate { it.key to it.title }
                .orEmpty()

        val done =
            guideTaskProgressRepository
                .findAllByJourneyId(journey.id)
                .filter { it.done }
                .map { progress ->
                    mapOf(
                        "taskKey" to progress.taskKey.value,
                        // 콘텐츠에서 사라진 할 일이면 제목을 모른다. 키는 남기고 제목만 비운다.
                        "title" to titleByKey[progress.taskKey],
                        "note" to progress.note,
                        "checkedAt" to progress.updatedAt.toString(),
                    )
                }

        return mapOf(
            "guideId" to journey.guideId.value,
            "title" to template?.title,
            "anchorDate" to journey.anchorDate.toString(),
            "startedAt" to journey.startedAt.toString(),
            "doneTasks" to done,
        )
    }
}
