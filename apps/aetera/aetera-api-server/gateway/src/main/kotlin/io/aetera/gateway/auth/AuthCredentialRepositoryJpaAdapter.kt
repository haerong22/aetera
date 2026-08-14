package io.aetera.gateway.auth

import io.aetera.gateway.common.saveMerging
import io.aetera.model.auth.AuthCredential
import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.AuthProvider
import io.aetera.model.user.UserId
import org.springframework.stereotype.Repository

@Repository
class AuthCredentialRepositoryJpaAdapter(
    private val authCredentialJpaRepository: AuthCredentialJpaRepository,
) : AuthCredentialRepository {
    override fun save(credential: AuthCredential): AuthCredential = authCredentialJpaRepository
        .saveMerging(
            id = credential.id.value,
            update = { it.applyFrom(credential) },
            create = { AuthCredentialJpaEntity.from(credential) },
        ).toModel()

    override fun getByUserIdAndProvider(
        userId: UserId,
        provider: AuthProvider,
    ): AuthCredential? = authCredentialJpaRepository.findByUserIdAndProvider(userId.value, provider)?.toModel()
}
