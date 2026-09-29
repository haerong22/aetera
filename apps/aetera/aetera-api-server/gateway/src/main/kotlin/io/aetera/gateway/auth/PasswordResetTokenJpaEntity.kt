package io.aetera.gateway.auth

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.auth.PasswordResetToken
import io.aetera.model.auth.PasswordResetTokenId
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetTokenJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "token_hash", nullable = false, length = 100, updatable = false)
    val tokenHash: String,
    @Column(name = "issued_at", nullable = false, updatable = false)
    val issuedAt: Instant,
    @Column(name = "expires_at", nullable = false, updatable = false)
    val expiresAt: Instant,
    @Column(name = "used_at")
    var usedAt: Instant?,
) : UuidJpaEntity(uid) {
    fun applyFrom(token: PasswordResetToken) {
        usedAt = token.usedAt
    }

    fun toModel(): PasswordResetToken = PasswordResetToken.reconstitute(
        id = PasswordResetTokenId(uid),
        userId = UserId(userId),
        tokenHash = tokenHash,
        issuedAt = issuedAt,
        expiresAt = expiresAt,
        usedAt = usedAt,
    )

    companion object {
        fun from(token: PasswordResetToken): PasswordResetTokenJpaEntity = PasswordResetTokenJpaEntity(
            uid = token.id.value,
            userId = token.userId.value,
            tokenHash = token.tokenHash,
            issuedAt = token.issuedAt,
            expiresAt = token.expiresAt,
            usedAt = token.usedAt,
        )
    }
}
