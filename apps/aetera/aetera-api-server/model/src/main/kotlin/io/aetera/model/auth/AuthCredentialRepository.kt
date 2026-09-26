package io.aetera.model.auth

import io.aetera.model.user.UserId

interface AuthCredentialRepository {
    fun save(credential: AuthCredential): AuthCredential

    /** 탈퇴할 때 한 번. 이 사용자의 것을 통째로 지운다. */
    fun deleteAllByUserId(userId: UserId)

    fun getByUserIdAndProvider(
        userId: UserId,
        provider: AuthProvider,
    ): AuthCredential?
}
