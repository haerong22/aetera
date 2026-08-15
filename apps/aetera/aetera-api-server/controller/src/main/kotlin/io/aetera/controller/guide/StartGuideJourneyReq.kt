package io.aetera.controller.guide

import io.aetera.usecase.guide.cmd.StartGuideJourneyCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class StartGuideJourneyReq(
    /**
     * 모든 마감의 기준이 되는 날짜(퇴사 예정일 등). 시각이 아니라 날짜다 —
     * 사용자가 달력에서 고른 그 날이 곧 값이므로 타임존 변환이 끼어들 여지가 없다.
     */
    @field:Schema(example = "2026-09-30")
    val anchorDate: LocalDate,
) {
    fun toCommand(
        userId: UUID,
        guideId: String,
    ): StartGuideJourneyCommand = StartGuideJourneyCommand(
        userId = userId,
        guideId = guideId,
        anchorDate = anchorDate,
    )
}
