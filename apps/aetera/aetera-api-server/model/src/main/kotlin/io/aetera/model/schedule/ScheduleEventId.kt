package io.aetera.model.schedule

import java.util.UUID

@JvmInline
value class ScheduleEventId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): ScheduleEventId = ScheduleEventId(UUID.randomUUID())
    }
}
