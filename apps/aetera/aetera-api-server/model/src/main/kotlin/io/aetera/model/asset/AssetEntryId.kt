package io.aetera.model.asset

import java.util.UUID

@JvmInline
value class AssetEntryId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): AssetEntryId = AssetEntryId(UUID.randomUUID())
    }
}
