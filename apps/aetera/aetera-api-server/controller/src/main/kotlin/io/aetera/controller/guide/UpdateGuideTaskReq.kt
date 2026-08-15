package io.aetera.controller.guide

import io.aetera.usecase.guide.cmd.UpdateGuideTaskCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import java.util.UUID

data class UpdateGuideTaskReq(
    val done: Boolean,
    @field:Size(max = 500)
    @field:Schema(example = "인사팀 김대리에게 확인함")
    val note: String? = null,
) {
    fun toCommand(
        userId: UUID,
        guideId: String,
        taskKey: String,
    ): UpdateGuideTaskCommand = UpdateGuideTaskCommand(
        userId = userId,
        guideId = guideId,
        taskKey = taskKey,
        done = done,
        note = note,
    )
}
