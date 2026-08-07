package io.aetera.gateway.auth

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.auth.RefreshToken
import io.aetera.model.auth.RefreshTokenId
import io.aetera.model.auth.RefreshTokenRevocation
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "token_hash", nullable = false, length = 100, updatable = false)
    val tokenHash: String,
    @Column(name = "issued_at", nullable = false, updatable = false)
    val issuedAt: Instant,
    @Column(name = "expires_at", nullable = false, updatable = false)
    val expiresAt: Instant,
    @Column(name = "revoked_at")
    var revokedAt: Instant?,
    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_reason", length = 20)
    var revokedReason: RefreshTokenRevocation?,
) : UuidJpaEntity(uid) {
    fun applyFrom(token: RefreshToken) {
        revokedAt = token.revokedAt
        revokedReason = token.revokedReason
    }

    fun toModel(): RefreshToken = RefreshToken.reconstitute(
        id = RefreshTokenId(uid),
        userId = UserId(userId),
        tokenHash = tokenHash,
        issuedAt = issuedAt,
        expiresAt = expiresAt,
        revokedAt = revokedAt,
        revokedReason = revokedReason,
    )

    companion object {
        fun from(token: RefreshToken): RefreshTokenJpaEntity = RefreshTokenJpaEntity(
            uid = token.id.value,
            userId = token.userId.value,
            tokenHash = token.tokenHash,
            issuedAt = token.issuedAt,
            expiresAt = token.expiresAt,
            revokedAt = token.revokedAt,
            revokedReason = token.revokedReason,
        )
    }
}
