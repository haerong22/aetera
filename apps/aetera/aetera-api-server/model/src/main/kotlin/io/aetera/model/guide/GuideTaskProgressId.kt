package io.aetera.model.guide

import java.util.UUID

@JvmInline
value class GuideTaskProgressId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): GuideTaskProgressId = GuideTaskProgressId(UUID.randomUUID())
    }
}
