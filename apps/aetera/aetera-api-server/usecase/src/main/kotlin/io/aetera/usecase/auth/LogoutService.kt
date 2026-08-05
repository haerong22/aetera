package io.aetera.usecase.auth

import io.aetera.model.auth.OpaqueToken
import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.auth.RefreshTokenRevocation
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class LogoutService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val clock: Clock,
) {
    /** 이미 없거나 만료된 토큰이어도 조용히 성공한다 — 로그아웃은 실패할 이유가 없다. */
    @Transactional
    fun logout(rawRefreshToken: String?) {
        if (rawRefreshToken.isNullOrBlank()) return
        val stored = refreshTokenRepository.getByTokenHash(OpaqueToken.hash(rawRefreshToken)) ?: return
        stored.revoke(clock.instant(), RefreshTokenRevocation.REVOKED)
        refreshTokenRepository.save(stored)
    }
}
