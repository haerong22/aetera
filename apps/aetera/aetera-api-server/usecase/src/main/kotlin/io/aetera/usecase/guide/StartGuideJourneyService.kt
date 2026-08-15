package io.aetera.usecase.guide

import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideJourney
import io.aetera.model.guide.GuideJourneyId
import io.aetera.model.guide.GuideJourneyRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.guide.cmd.StartGuideJourneyCommand
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * 여정 시작 — 그리고 기준일 변경. 둘을 한 유스케이스로 두는 이유는 사용자에게 같은 행동이기 때문이다
 * ("퇴사일을 이걸로 한다"). 퇴사일이 밀리는 건 흔한 일이라 시작만큼 자주 일어난다.
 *
 * 몇 번을 눌러도 같은 결과이고, 기준일을 바꿔도 체크해 둔 항목은 그대로 남는다 —
 * 마감일만 통째로 움직인다.
 */
@Service
class StartGuideJourneyService(
    private val guideCatalog: GuideCatalog,
    private val guideJourneyRepository: GuideJourneyRepository,
    private val findGuideService: FindGuideService,
    private val clock: Clock,
) {
    @Transactional
    fun start(command: StartGuideJourneyCommand): GuideViewDto {
        val template = guideCatalog.getOrThrow(GuideId(command.guideId))
        val owner = UserId(command.userId)
        val now = clock.instant()

        val journey =
            guideJourneyRepository
                .getByUserIdAndGuideId(owner, template.id)
                ?.apply { changeAnchorDate(command.anchorDate, now) }
                ?: GuideJourney.start(
                    id = GuideJourneyId.next(),
                    userId = owner,
                    guideId = template.id,
                    anchorDate = command.anchorDate,
                    now = now,
                )
        val saved = guideJourneyRepository.save(journey)

        log.info { "가이드 여정 설정 userId=$owner guideId=${template.id} anchorDate=${command.anchorDate}" }
        return findGuideService.viewOf(template, saved)
    }
}
