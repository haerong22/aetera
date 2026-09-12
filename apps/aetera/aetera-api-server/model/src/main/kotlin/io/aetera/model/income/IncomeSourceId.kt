package io.aetera.model.income

import java.util.UUID

@JvmInline
value class IncomeSourceId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): IncomeSourceId = IncomeSourceId(UUID.randomUUID())
    }
}
