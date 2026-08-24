package io.aetera.model.goal

import java.util.UUID

@JvmInline
value class GoalId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): GoalId = GoalId(UUID.randomUUID())
    }
}
