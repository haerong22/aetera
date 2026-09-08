package io.aetera.controller.timeline

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.timeline.FindTimelineService
import io.aetera.usecase.timeline.TimelineEntryDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

/**
 * 타임라인 모듈의 API. `/api/v1/modules/timeline/..` 아래라 활성화 검사는 코어 가드가 한다.
 *
 * 읽기 하나뿐이다. 타임라인은 스스로 저장하는 게 없고, 켠 모듈들이 이미 가진 것을 모아 보여줄 뿐이다.
 */
@RestController
@RequestMapping("/api/v1/modules/timeline/entries")
@Tag(name = "Timeline")
class TimelineController(
    private val findTimelineService: FindTimelineService,
) {
    /**
     * 커서 대신 기간으로 자른다. 여러 출처를 커서로 합치려면 각 출처에서 넉넉히 당겨 와야 하고,
     * 그러면 어디까지 읽었는지가 지저분해진다. 화면은 "2026년 / 2025년" 으로 넘긴다.
     */
    @GetMapping
    @Operation(summary = "기간 안의 타임라인. 최근 것이 위에 온다.")
    fun findTimeline(
        @CurrentUserId userId: UUID,
        @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
    ): List<TimelineEntryDto> = findTimelineService.findTimeline(userId, from, to)
}
