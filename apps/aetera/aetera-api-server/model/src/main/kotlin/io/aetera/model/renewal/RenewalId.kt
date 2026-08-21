package io.aetera.model.renewal

import java.util.UUID

@JvmInline
value class RenewalId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): RenewalId = RenewalId(UUID.randomUUID())
    }
}
