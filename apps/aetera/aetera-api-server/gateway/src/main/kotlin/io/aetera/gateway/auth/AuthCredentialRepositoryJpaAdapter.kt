package io.aetera.gateway.auth

import io.aetera.model.auth.AuthCredential
import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.AuthProvider
import io.aetera.model.user.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class AuthCredentialRepositoryJpaAdapter(
    private val authCredentialJpaRepository: AuthCredentialJpaRepository,
) : AuthCredentialRepository {
    override fun save(credential: AuthCredential): AuthCredential {
        val entity =
            authCredentialJpaRepository.findByIdOrNull(credential.id.value)?.apply { applyFrom(credential) }
                ?: AuthCredentialJpaEntity.from(credential)
        return authCredentialJpaRepository.save(entity).toModel()
    }

    override fun getByUserIdAndProvider(
        userId: UserId,
        provider: AuthProvider,
    ): AuthCredential? = authCredentialJpaRepository.findByUserIdAndProvider(userId.value, provider)?.toModel()
}
