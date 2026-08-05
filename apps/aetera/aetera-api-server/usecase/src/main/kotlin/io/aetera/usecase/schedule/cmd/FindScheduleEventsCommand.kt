package io.aetera.usecase.schedule.cmd

import io.aetera.model.schedule.SchedulePeriod
import java.time.Instant
import java.util.UUID

data class FindScheduleEventsCommand(
    val userId: UUID,
    val from: Instant,
    val to: Instant,
) {
    fun toPeriod(): SchedulePeriod = SchedulePeriod(from, to)
}
