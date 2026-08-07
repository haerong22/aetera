package io.aetera.gateway.auth

import io.aetera.model.auth.RefreshToken
import io.aetera.model.auth.RefreshTokenId
import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.auth.RefreshTokenRevocation
import io.aetera.model.user.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class RefreshTokenRepositoryJpaAdapter(
    private val refreshTokenJpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun save(token: RefreshToken): RefreshToken {
        val entity =
            refreshTokenJpaRepository.findByIdOrNull(token.id.value)?.apply { applyFrom(token) }
                ?: RefreshTokenJpaEntity.from(token)
        return refreshTokenJpaRepository.save(entity).toModel()
    }

    override fun getByTokenHash(tokenHash: String): RefreshToken? = refreshTokenJpaRepository.findByTokenHash(tokenHash)?.toModel()

    override fun markRotated(
        id: RefreshTokenId,
        at: Instant,
    ): Boolean = refreshTokenJpaRepository.markRotated(id.value, at, RefreshTokenRevocation.ROTATED) == 1

    override fun revokeAllByUserId(
        userId: UserId,
        at: Instant,
    ) {
        refreshTokenJpaRepository.revokeAllByUserId(userId.value, at, RefreshTokenRevocation.REVOKED)
    }
}
