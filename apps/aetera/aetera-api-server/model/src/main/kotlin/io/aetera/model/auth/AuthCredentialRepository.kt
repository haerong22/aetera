package io.aetera.model.auth

import io.aetera.model.user.UserId

interface AuthCredentialRepository {
    fun save(credential: AuthCredential): AuthCredential

    fun getByUserIdAndProvider(
        userId: UserId,
        provider: AuthProvider,
    ): AuthCredential?
}
