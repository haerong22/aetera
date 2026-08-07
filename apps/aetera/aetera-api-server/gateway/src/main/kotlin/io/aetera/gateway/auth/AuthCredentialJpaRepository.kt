package io.aetera.gateway.auth

import io.aetera.model.auth.AuthProvider
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AuthCredentialJpaRepository : JpaRepository<AuthCredentialJpaEntity, UUID> {
    fun findByUserIdAndProvider(
        userId: UUID,
        provider: AuthProvider,
    ): AuthCredentialJpaEntity?
}
