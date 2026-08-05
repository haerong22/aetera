package io.aetera.model.module

import java.util.UUID

@JvmInline
value class ModuleEnrollmentId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): ModuleEnrollmentId = ModuleEnrollmentId(UUID.randomUUID())
    }
}
