package io.aetera.gateway.auth

import io.aetera.gateway.common.saveMerging
import io.aetera.model.auth.PasswordResetToken
import io.aetera.model.auth.PasswordResetTokenRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class PasswordResetTokenRepositoryJpaAdapter(
    private val passwordResetTokenJpaRepository: PasswordResetTokenJpaRepository,
) : PasswordResetTokenRepository {
    override fun save(token: PasswordResetToken): PasswordResetToken = passwordResetTokenJpaRepository
        .saveMerging(
            id = token.id.value,
            update = { it.applyFrom(token) },
            create = { PasswordResetTokenJpaEntity.from(token) },
        ).toModel()

    override fun getByTokenHash(tokenHash: String): PasswordResetToken? = passwordResetTokenJpaRepository
        .findByTokenHash(tokenHash)
        ?.toModel()

    override fun markAllUsedByUserId(
        userId: UserId,
        at: Instant,
    ) {
        passwordResetTokenJpaRepository.markAllUsedByUserId(userId.value, at)
    }

    override fun deleteAllByUserId(userId: UserId) {
        passwordResetTokenJpaRepository.deleteAllByUserId(userId.value)
    }
}
