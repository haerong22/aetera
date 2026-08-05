package io.aetera.usecase.schedule.cmd

import java.time.Instant
import java.util.UUID

data class UpdateScheduleEventCommand(
    val userId: UUID,
    val eventId: UUID,
    val title: String,
    val description: String?,
    val startsAt: Instant,
    val endsAt: Instant,
    val allDay: Boolean,
    val color: String?,
)
