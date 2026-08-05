package io.aetera.usecase.schedule

import io.aetera.model.schedule.ScheduleEvent
import java.time.Instant
import java.util.UUID

data class ScheduleEventDto(
    val id: UUID,
    val title: String,
    val description: String?,
    val startsAt: Instant,
    val endsAt: Instant,
    val allDay: Boolean,
    val color: String?,
    val createdAt: Instant,
) {
    constructor(event: ScheduleEvent) : this(
        id = event.id.value,
        title = event.title,
        description = event.description,
        startsAt = event.startsAt,
        endsAt = event.endsAt,
        allDay = event.allDay,
        color = event.color,
        createdAt = event.createdAt,
    )
}
