package io.aetera.usecase.guide

import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideJourneyRepository
import io.aetera.model.guide.GuideTaskProgressRepository
import io.aetera.model.user.UserId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * 여정 초기화. 기준일과 체크 상태를 모두 버리고 시작 전으로 되돌린다.
 *
 * 모듈 비활성화(소프트)와 다르다 — 저쪽은 데이터를 남기고 접근만 막는 것이고,
 * 이건 사용자가 "처음부터 다시"를 고른 것이므로 실제로 지운다.
 */
@Service
class ResetGuideJourneyService(
    private val guideCatalog: GuideCatalog,
    private val guideJourneyRepository: GuideJourneyRepository,
    private val guideTaskProgressRepository: GuideTaskProgressRepository,
    private val findGuideService: FindGuideService,
) {
    /** 시작한 적 없어도 성공이다 — 초기화의 결과("시작 전 상태")는 어느 쪽이든 같다. */
    @Transactional
    fun reset(
        userId: UUID,
        guideId: String,
    ): GuideViewDto {
        val template = guideCatalog.getOrThrow(GuideId(guideId))

        guideJourneyRepository.getByUserIdAndGuideId(UserId(userId), template.id)?.let { journey ->
            // 진행 행이 여정을 참조하므로 순서를 지킨다.
            guideTaskProgressRepository.deleteAllByJourneyId(journey.id)
            guideJourneyRepository.delete(journey)
            log.info { "가이드 여정 초기화 userId=$userId guideId=${template.id}" }
        }

        // 지운 뒤의 화면은 언제나 "시작 전"이다. 진행 상태를 다시 읽을 필요조차 없다.
        return findGuideService.viewOf(template, null)
    }
}
