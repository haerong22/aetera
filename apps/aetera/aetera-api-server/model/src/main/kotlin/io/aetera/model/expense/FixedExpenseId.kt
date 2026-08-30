package io.aetera.model.expense

import java.util.UUID

@JvmInline
value class FixedExpenseId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): FixedExpenseId = FixedExpenseId(UUID.randomUUID())
    }
}
