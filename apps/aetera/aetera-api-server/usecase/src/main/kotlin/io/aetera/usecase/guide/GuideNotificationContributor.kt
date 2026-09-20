package io.aetera.usecase.guide

import io.aetera.model.guide.GuideJourney
import io.aetera.model.guide.GuideJourneyRepository
import io.aetera.model.guide.GuideTask
import io.aetera.model.guide.GuideTaskProgressRepository
import io.aetera.model.module.ModuleId
import io.aetera.model.module.Notice
import io.aetera.model.module.NotificationContributor
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 마감이 지난 가이드 할 일을 알린다.
 *
 * 가이드는 날짜가 박힌 할 일 수십 개다(퇴사 준비는 27개). 기준일을 넣으면 마감이 정해지는데,
 * 지금까지는 **앱을 열어야만 그 날짜가 보였다** — 몇 달에 걸친 일이라 그 사이에 조용히 지나간다.
 *
 * 무엇을 안 보내는지가 더 중요하다:
 *
 * - **미리 알리지 않는다.** "3일 뒤 마감"까지 보내면 27개짜리 가이드에서 거의 매일 메일이 오고,
 *   그러면 열어 보지 않게 되어 정작 놓친 것도 함께 묻힌다. 지났을 때 재촉하는 편이 낫다.
 * - **선택 항목은 빼고 필수만.** 안 해도 되는 일로 재촉할 이유가 없다.
 * - **[STALE_AFTER_DAYS] 이 지난 것은 뺀다.** 체크해야만 사라지는 줄이라, 그냥 두면 같은
 *   잔소리가 영원히 매일 간다. 한 달을 넘겼으면 그 항목은 접은 것으로 본다.
 * - **[MAX_LINES] 줄까지.** 밀린 게 열 개여도 메일이 할 일 목록이 되면 안 된다.
 *   가장 오래된 것부터 보여 준다 — 밀린 순서대로 처리하는 게 자연스럽다.
 *
 * 가이드 하나가 곧 모듈 하나인데 읽는 방식은 넷이 같아서 기여자는 하나다
 * ([GuideTimelineContributor] 와 같은 이유).
 */
@Component
class GuideNotificationContributor(
    private val guideJourneyRepository: GuideJourneyRepository,
    private val guideTaskProgressRepository: GuideTaskProgressRepository,
    private val guideCatalog: GuideCatalog,
) : NotificationContributor {
    private companion object {
        /** 이보다 오래 밀린 항목은 접은 것으로 본다. */
        const val STALE_AFTER_DAYS = 30L

        /** 한 가이드가 한 번에 낼 수 있는 줄 수. */
        const val MAX_LINES = 3
    }

    override val moduleIds: Set<ModuleId> = guideCatalog.guideIds.map { it.toModuleId() }.toSet()

    override fun noticesFor(
        userId: UserId,
        today: LocalDate,
    ): List<Notice> = guideJourneyRepository
        .findAllByUserId(userId)
        .flatMap { journey -> noticesIn(journey, today) }

    private fun noticesIn(
        journey: GuideJourney,
        today: LocalDate,
    ): List<Notice> {
        // 콘텐츠에서 사라진 가이드의 낡은 여정이 남아 있을 수 있다.
        val template = guideCatalog.findOrNull(journey.guideId) ?: return emptyList()
        val done =
            guideTaskProgressRepository
                .findAllByJourneyId(journey.id)
                .filter { it.done }
                .map { it.taskKey }
                .toSet()

        return template.phases
            .flatMap { it.tasks }
            .filter { it.required && it.key !in done }
            .mapNotNull { task -> overdueOrNull(task, journey.anchorDate, today) }
            .sortedBy { it.dueDate }
            .take(MAX_LINES)
            .map { notice(journey, template.title, it) }
    }

    /** 재촉할 것이면 [Overdue], 아니면 null. 마감 전이거나 접은 것으로 볼 만큼 오래 밀렸으면 뺀다. */
    private fun overdueOrNull(
        task: GuideTask,
        anchorDate: LocalDate,
        today: LocalDate,
    ): Overdue? {
        val dueDate = task.dueDateFrom(anchorDate)
        val days = today.toEpochDay() - dueDate.toEpochDay()
        return if (days in 0..STALE_AFTER_DAYS) Overdue(task, dueDate, days) else null
    }

    private fun notice(
        journey: GuideJourney,
        guideTitle: String,
        overdue: Overdue,
    ): Notice = Notice(
        moduleId = journey.guideId.toModuleId(),
        on = overdue.dueDate,
        title = overdue.task.title,
        // 어느 가이드의 할 일인지 밝힌다 — 가이드를 둘 이상 켜면 제목만으로는 알 수 없다.
        detail = "$guideTitle · ${overdue.label}",
    )
}

/**
 * 마감이 지난 할 일 하나. **지난 날수를 이미 재 두었다.**
 *
 * 날짜를 넘기고 받는 쪽이 다시 세게 하면, 거르기를 통과했다는 사실이 전해지지 않는다 —
 * 접은 것으로 본 항목에도 "31일 지났어요"를 붙일 수 있게 된다.
 */
private data class Overdue(
    val task: GuideTask,
    val dueDate: LocalDate,
    val days: Long,
) {
    val label: String get() = if (days == 0L) "오늘까지예요" else "${days}일 지났어요"
}
