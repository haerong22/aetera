package io.aetera.usecase.guide

import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideJourney
import io.aetera.model.guide.GuideJourneyRepository
import io.aetera.model.guide.GuideTaskProgressRepository
import io.aetera.model.guide.GuideTemplate
import io.aetera.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 가이드 화면 조립. 조회 API 이자 **모든 변경 API 의 응답을 만드는 곳**이기도 하다 —
 * 변경 후에 다시 조회하러 오지 않아도 되도록 바뀐 전체 상태를 그대로 돌려준다.
 */
@Service
@Transactional(readOnly = true)
class FindGuideService(
    private val guideCatalog: GuideCatalog,
    private val guideJourneyRepository: GuideJourneyRepository,
    private val guideTaskProgressRepository: GuideTaskProgressRepository,
) {
    fun findGuide(
        userId: UUID,
        guideId: String,
    ): GuideViewDto {
        val template = guideCatalog.getOrThrow(GuideId(guideId))
        return viewOf(template, guideJourneyRepository.getByUserIdAndGuideId(UserId(userId), template.id))
    }

    /**
     * 이미 손에 든 여정으로 화면을 만든다.
     *
     * 변경 유스케이스는 방금 여정을 읽어 놓고도 [findGuide] 를 부르면 같은 여정을 한 번 더
     * 조회하게 된다(JPQL 이라 1차 캐시로 합쳐지지 않는다). 그래서 그쪽이 쓸 입구를 따로 둔다.
     * 진행 상태는 방금 바뀌었을 수 있으니 여기서 다시 읽는다.
     */
    fun viewOf(
        template: GuideTemplate,
        journey: GuideJourney?,
    ): GuideViewDto {
        // 시작 전에는 진행 행이 있을 수 없다 — 없는 여정으로 조회하지 않는다.
        val progresses = journey?.let { guideTaskProgressRepository.findAllByJourneyId(it.id) }.orEmpty()
        return GuideViewDto.of(template, journey, progresses)
    }
}
