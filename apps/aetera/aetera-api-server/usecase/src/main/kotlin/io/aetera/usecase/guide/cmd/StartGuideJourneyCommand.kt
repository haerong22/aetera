package io.aetera.usecase.guide.cmd

import java.time.LocalDate
import java.util.UUID

data class StartGuideJourneyCommand(
    val userId: UUID,
    val guideId: String,
    val anchorDate: LocalDate,
)
