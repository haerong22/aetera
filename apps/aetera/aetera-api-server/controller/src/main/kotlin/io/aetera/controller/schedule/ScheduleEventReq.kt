package io.aetera.controller.schedule

import io.aetera.usecase.schedule.cmd.CreateScheduleEventCommand
import io.aetera.usecase.schedule.cmd.UpdateScheduleEventCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class ScheduleEventReq(
    @field:NotBlank
    @field:Size(max = 200)
    @field:Schema(example = "팀 회의")
    val title: String,
    @field:Size(max = 2000)
    val description: String? = null,
    @field:Schema(example = "2026-03-01T09:00:00Z")
    val startsAt: Instant,
    @field:Schema(example = "2026-03-01T10:00:00Z")
    val endsAt: Instant,
    val allDay: Boolean = false,
    @field:Pattern(regexp = "^#[0-9a-fA-F]{6}$")
    @field:Schema(example = "#3182f6")
    val color: String? = null,
) {
    fun toCreateCommand(userId: UUID): CreateScheduleEventCommand = CreateScheduleEventCommand(
        userId = userId,
        title = title,
        description = description,
        startsAt = startsAt,
        endsAt = endsAt,
        allDay = allDay,
        color = color,
    )

    fun toUpdateCommand(
        userId: UUID,
        eventId: UUID,
    ): UpdateScheduleEventCommand = UpdateScheduleEventCommand(
        userId = userId,
        eventId = eventId,
        title = title,
        description = description,
        startsAt = startsAt,
        endsAt = endsAt,
        allDay = allDay,
        color = color,
    )
}
