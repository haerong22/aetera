package io.aetera.model.spending

import java.util.UUID

@JvmInline
value class SpendingRecordId(
    val value: UUID,
) {
    companion object {
        fun next(): SpendingRecordId = SpendingRecordId(UUID.randomUUID())
    }
}
