package io.aetera.model.guide

import java.util.UUID

@JvmInline
value class GuideJourneyId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): GuideJourneyId = GuideJourneyId(UUID.randomUUID())
    }
}
