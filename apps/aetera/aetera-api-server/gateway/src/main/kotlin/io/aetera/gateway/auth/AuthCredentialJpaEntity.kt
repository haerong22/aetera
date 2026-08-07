package io.aetera.gateway.auth

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.auth.AuthCredential
import io.aetera.model.auth.AuthCredentialId
import io.aetera.model.auth.AuthProvider
import io.aetera.model.auth.EncryptedPassword
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "auth_credentials")
class AuthCredentialJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    val provider: AuthProvider,
    @Column(name = "provider_user_id", length = 100)
    val providerUserId: String?,
    @Column(name = "password_hash", length = 300)
    var passwordHash: String?,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
) : UuidJpaEntity(uid) {
    fun applyFrom(credential: AuthCredential) {
        passwordHash = credential.passwordHash?.value
    }

    fun toModel(): AuthCredential = AuthCredential.reconstitute(
        id = AuthCredentialId(uid),
        userId = UserId(userId),
        provider = provider,
        providerUserId = providerUserId,
        passwordHash = passwordHash?.let(::EncryptedPassword),
        createdAt = createdAt,
    )

    companion object {
        fun from(credential: AuthCredential): AuthCredentialJpaEntity = AuthCredentialJpaEntity(
            uid = credential.id.value,
            userId = credential.userId.value,
            provider = credential.provider,
            providerUserId = credential.providerUserId,
            passwordHash = credential.passwordHash?.value,
            createdAt = credential.createdAt,
        )
    }
}
