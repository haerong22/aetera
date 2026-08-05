package io.aetera.model.auth

import java.util.UUID

@JvmInline
value class AuthCredentialId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): AuthCredentialId = AuthCredentialId(UUID.randomUUID())
    }
}
