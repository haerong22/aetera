package io.aetera.model.guide

import java.time.LocalDate

/**
 * 가이드 콘텐츠. **사용자별 데이터가 아니라 배포물이다** — 모듈이 코드로 존재하듯 콘텐츠도 코드로 존재한다.
 *
 * DB 에 두지 않는 이유는 [io.aetera.usecase.module.ModuleRegistry] 와 같다: 콘텐츠는 그것을 렌더할
 * 코드가 배포되어 있어야 의미가 있고, 버전 관리·리뷰·테스트를 그대로 받는 편이 낫다.
 * 편집 화면이 필요할 만큼 자주 바뀌기 시작하면 그때 옮긴다.
 *
 * 개인화되는 것은 딱 두 가지다: [GuideJourney.anchorDate] 기준으로 계산되는 각 할 일의 마감일과,
 * [GuideTaskProgress] 로 저장되는 체크·메모.
 */
data class GuideTemplate(
    val id: GuideId,
    val title: String,
    val summary: String,
    /** 모든 마감의 기준이 되는 날짜의 이름. 퇴사 가이드라면 "퇴사 예정일". */
    val anchorLabel: String,
    /** 법·회사 규정처럼 상황에 따라 달라지는 부분에 대한 고지. 화면 하단에 그대로 노출한다. */
    val disclaimer: String,
    val phases: List<GuidePhase>,
) {
    val tasks: List<GuideTask> = phases.flatMap { it.tasks }

    private val tasksByKey: Map<GuideTaskKey, GuideTask> = tasks.associateBy { it.key }

    init {
        require(phases.isNotEmpty()) { "가이드 '$id' 에 단계가 없습니다." }
        // 키가 겹치면 두 항목의 체크 상태가 한 행을 공유해 서로를 덮어쓴다. 기동 시점에 잡는다.
        val duplicated =
            tasks
                .groupingBy { it.key }
                .eachCount()
                .filterValues { it > 1 }
                .keys
        require(duplicated.isEmpty()) { "가이드 '$id' 의 할 일 키가 겹칩니다: $duplicated" }
    }

    fun hasTask(key: GuideTaskKey): Boolean = tasksByKey.containsKey(key)

    /** 필수 항목만 진행률에 반영한다 — 참고용 항목까지 세면 "다 했다"에 도달할 수 없다. */
    val requiredTaskCount: Int = tasks.count { it.required }
}

/**
 * 가이드의 한 단계. 시간 순서대로 나열되며 화면에서 섹션 하나가 된다.
 */
data class GuidePhase(
    val key: String,
    val title: String,
    val summary: String,
    val tasks: List<GuideTask>,
)

/**
 * 할 일 하나.
 *
 * [dueOffsetDays] 는 기준일(D-day) 대비 상대 일수다 — 음수면 기준일 전, 양수면 후.
 * 절대 날짜를 콘텐츠에 박으면 모든 사용자가 남의 일정표를 보게 되므로 상대값으로만 둔다.
 */
data class GuideTask(
    val key: GuideTaskKey,
    val title: String,
    val description: String,
    val dueOffsetDays: Int,
    /** 진행률에 반영되는 항목인지. 놓치면 돈이나 권리를 잃는 것들만 필수로 둔다. */
    val required: Boolean = true,
    val link: GuideLink? = null,
) {
    fun dueDateFrom(anchorDate: LocalDate): LocalDate = anchorDate.plusDays(dueOffsetDays.toLong())
}

/** 할 일과 관련된 공식 창구. 사용자가 "그래서 어디서 하지"를 다시 검색하지 않게 한다. */
data class GuideLink(
    val label: String,
    val url: String,
)
