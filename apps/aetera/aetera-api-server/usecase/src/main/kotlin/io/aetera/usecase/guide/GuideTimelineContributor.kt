package io.aetera.usecase.guide

import io.aetera.model.guide.GuideJourneyRepository
import io.aetera.model.module.ModuleId
import io.aetera.model.module.TimelineContributor
import io.aetera.model.module.TimelineEntry
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 가이드의 기준일을 타임라인에 낸다 — 퇴사일, 이사일, 입사 예정일, 정산 기준일.
 *
 * **가이드 하나가 곧 모듈 하나**인데 읽는 방식은 넷이 같아서 기여자는 하나다.
 * 그래서 [moduleIds] 로 넷을 함께 말하고, 낸 줄마다 자기 가이드를 가리킨다 —
 * 코어가 켜지지 않은 가이드의 줄을 거기서 걸러 낸다.
 *
 * 체크한 할 일은 내지 않는다. 27개짜리 가이드를 끝내면 타임라인이 그것만으로 덮인다.
 * 실을 만한 건 "그래서 언제 퇴사했나" 하나다.
 */
@Component
class GuideTimelineContributor(
    private val guideJourneyRepository: GuideJourneyRepository,
    private val guideCatalog: GuideCatalog,
) : TimelineContributor {
    override val moduleIds: Set<ModuleId> = guideCatalog.guideIds.map { it.toModuleId() }.toSet()

    override fun entriesIn(
        userId: UserId,
        from: LocalDate,
        to: LocalDate,
    ): List<TimelineEntry> = guideJourneyRepository
        .findAllByUserId(userId)
        // 자산·만기와 달리 기간을 쿼리로 내리지 않는다. 여정은 가이드마다 최대 하나라
        // 한 사람의 행이 배포된 가이드 수(지금 넷)를 넘지 않고, anchor_date 에 인덱스도 없다.
        .filter { it.anchorDate in from..to }
        .mapNotNull { journey ->
            // 콘텐츠에서 사라진 가이드의 낡은 여정이 남아 있을 수 있다.
            val template = guideCatalog.findOrNull(journey.guideId) ?: return@mapNotNull null
            TimelineEntry(
                moduleId = journey.guideId.toModuleId(),
                on = journey.anchorDate,
                title = template.anchorLabel,
                detail = template.title,
            )
        }
}
