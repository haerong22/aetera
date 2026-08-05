package io.aetera.model.auth

import java.util.UUID

@JvmInline
value class RefreshTokenId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): RefreshTokenId = RefreshTokenId(UUID.randomUUID())
    }
}
