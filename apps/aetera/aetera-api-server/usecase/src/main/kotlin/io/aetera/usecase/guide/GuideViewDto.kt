package io.aetera.usecase.guide

import io.aetera.model.guide.GuideJourney
import io.aetera.model.guide.GuideLink
import io.aetera.model.guide.GuidePhase
import io.aetera.model.guide.GuideTask
import io.aetera.model.guide.GuideTaskKey
import io.aetera.model.guide.GuideTaskProgress
import io.aetera.model.guide.GuideTemplate
import java.time.Instant
import java.time.LocalDate

/**
 * 가이드 화면 하나를 그리는 데 필요한 전부. 콘텐츠 ⊕ 내 여정 ⊕ 내 체크 상태.
 *
 * 마감일은 서버가 계산해서 내려준다 — 상대 일수를 브라우저가 더하게 두면 가이드마다 같은 계산이
 * 반복되고, 규칙이 바뀔 때 두 곳을 고쳐야 한다.
 *
 * 반대로 "오늘까지 며칠 남았나"는 내려보내지 **않는다**. 그건 사용자의 로컬 날짜를 알아야 하는데
 * 서버는 그걸 모른다. [LocalDate] 만 주면 브라우저가 자기 달력으로 비교하므로 언제나 맞다.
 */
data class GuideViewDto(
    val guideId: String,
    val title: String,
    val summary: String,
    val anchorLabel: String,
    val disclaimer: String,
    /** null 이면 아직 시작하지 않은 가이드 — 오류가 아니라 정상적인 시작 전 상태다. */
    val journey: GuideJourneyDto?,
    val phases: List<GuidePhaseDto>,
    val progress: GuideProgressDto,
) {
    companion object {
        fun of(
            template: GuideTemplate,
            journey: GuideJourney?,
            progresses: List<GuideTaskProgress>,
        ): GuideViewDto {
            val byKey: Map<GuideTaskKey, GuideTaskProgress> = progresses.associateBy { it.taskKey }
            val phases = template.phases.map { phase -> GuidePhaseDto.of(phase, journey?.anchorDate, byKey) }
            return GuideViewDto(
                guideId = template.id.value,
                title = template.title,
                summary = template.summary,
                anchorLabel = template.anchorLabel,
                disclaimer = template.disclaimer,
                journey = journey?.let(GuideJourneyDto::of),
                phases = phases,
                progress = GuideProgressDto.of(template, byKey),
            )
        }
    }
}

/**
 * 여정에서 화면이 실제로 쓰는 것. `startedAt` 은 담지 않는다 —
 * 읽는 곳이 없는 개인 정보를 응답에 실을 이유가 없다(필요해지면 그때 넣는다).
 */
data class GuideJourneyDto(
    val anchorDate: LocalDate,
) {
    companion object {
        fun of(journey: GuideJourney): GuideJourneyDto = GuideJourneyDto(journey.anchorDate)
    }
}

data class GuidePhaseDto(
    val key: String,
    val title: String,
    val summary: String,
    val tasks: List<GuideTaskDto>,
) {
    companion object {
        fun of(
            phase: GuidePhase,
            anchorDate: LocalDate?,
            progresses: Map<GuideTaskKey, GuideTaskProgress>,
        ): GuidePhaseDto = GuidePhaseDto(
            key = phase.key,
            title = phase.title,
            summary = phase.summary,
            tasks = phase.tasks.map { GuideTaskDto.of(it, anchorDate, progresses[it.key]) },
        )
    }
}

data class GuideTaskDto(
    val key: String,
    val title: String,
    val description: String,
    /** 기준일 대비 상대 일수. 여정을 시작하기 전에도 "D-30 쯤" 을 보여줄 수 있게 함께 준다. */
    val dueOffsetDays: Int,
    /** 여정을 시작해야 정해진다. 시작 전에는 null. */
    val dueDate: LocalDate?,
    val required: Boolean,
    val link: GuideLinkDto?,
    val done: Boolean,
    val note: String?,
) {
    companion object {
        fun of(
            task: GuideTask,
            anchorDate: LocalDate?,
            progress: GuideTaskProgress?,
        ): GuideTaskDto = GuideTaskDto(
            key = task.key.value,
            title = task.title,
            description = task.description,
            dueOffsetDays = task.dueOffsetDays,
            dueDate = anchorDate?.let(task::dueDateFrom),
            required = task.required,
            link = task.link?.let(GuideLinkDto::of),
            done = progress?.done ?: false,
            note = progress?.note,
        )
    }
}

data class GuideLinkDto(
    val label: String,
    val url: String,
) {
    companion object {
        fun of(link: GuideLink): GuideLinkDto = GuideLinkDto(link.label, link.url)
    }
}

/**
 * 진행률. 필수/전체를 따로 준다 — 사용자가 보는 "완료"는 필수 기준이고,
 * 전체는 참고용 항목까지 얼마나 봤는지를 보여준다.
 */
data class GuideProgressDto(
    val total: Int,
    val done: Int,
    val requiredTotal: Int,
    val requiredDone: Int,
) {
    companion object {
        fun of(
            template: GuideTemplate,
            progresses: Map<GuideTaskKey, GuideTaskProgress>,
        ): GuideProgressDto {
            // 콘텐츠에서 사라진 항목의 낡은 행이 남아 있을 수 있으므로, 세는 기준은 언제나 템플릿이다.
            val doneTasks = template.tasks.filter { progresses[it.key]?.done == true }
            return GuideProgressDto(
                total = template.tasks.size,
                done = doneTasks.size,
                requiredTotal = template.requiredTaskCount,
                requiredDone = doneTasks.count { it.required },
            )
        }
    }
}
