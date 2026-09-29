package io.aetera.model.auth

import java.util.UUID

@JvmInline
value class PasswordResetTokenId(
    val value: UUID,
) {
    companion object {
        fun next(): PasswordResetTokenId = PasswordResetTokenId(UUID.randomUUID())
    }
}
