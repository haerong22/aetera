package io.aetera.usecase.timeline

import io.aetera.model.module.ModuleEnrollmentRepository
import io.aetera.model.module.TimelineContributor
import io.aetera.model.timeline.TimelineErrorCode
import io.aetera.model.user.UserId
import io.aetera.shared.error.ensure
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

/**
 * 켠 모듈들이 내놓은 줄을 시간순으로 합친다.
 *
 * **어떤 모듈이 있는지 모른다.** 스프링이 [TimelineContributor] 빈을 전부 넣어 주고,
 * 이 클래스는 그중 사용자가 켠 것만 골라 부른다 — 모듈이 늘어도 여기는 그대로다.
 *
 * 꺼진 모듈은 부르지 않는다. 끈 모듈이 다른 화면에서 계속 일하고 있으면
 * "중지"가 무슨 뜻인지 알 수 없게 된다.
 */
@Service
@Transactional(readOnly = true)
class FindTimelineService(
    private val contributors: List<TimelineContributor>,
    private val moduleEnrollmentRepository: ModuleEnrollmentRepository,
) {
    fun findTimeline(
        userId: UUID,
        from: LocalDate,
        to: LocalDate,
    ): List<TimelineEntryDto> {
        ensure(!from.isAfter(to), TimelineErrorCode.INVALID_RANGE, "시작일이 종료일보다 늦습니다. from=$from to=$to")

        val owner = UserId(userId)
        val enabled =
            moduleEnrollmentRepository
                .findAllByUserId(owner)
                .filter { it.isEnabled }
                .map { it.moduleId }
                .toSet()

        return contributors
            .filter { contributor -> contributor.moduleIds.any { it in enabled } }
            .flatMap { it.entriesIn(owner, from, to) }
            // 기여자 하나가 여러 모듈을 낼 수 있으므로(가이드) 줄 단위로 한 번 더 거른다.
            .filter { it.moduleId in enabled }
            .map(::TimelineEntryDto)
            // 같은 날이면 모듈 아이디로 고정한다 — tiebreaker 가 없으면 같은 요청이 매번 다른 순서를 준다.
            .sortedWith(compareByDescending<TimelineEntryDto> { it.on }.thenBy { it.moduleId }.thenBy { it.title })
    }
}
