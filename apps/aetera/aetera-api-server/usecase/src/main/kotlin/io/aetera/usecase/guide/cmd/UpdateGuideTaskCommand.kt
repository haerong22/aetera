package io.aetera.usecase.guide.cmd

import java.util.UUID

data class UpdateGuideTaskCommand(
    val userId: UUID,
    val guideId: String,
    val taskKey: String,
    val done: Boolean,
    val note: String?,
)
