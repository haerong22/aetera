package io.aetera.usecase.auth

import io.aetera.model.auth.AccessTokenProvider
import io.aetera.model.auth.OpaqueToken
import io.aetera.model.auth.RefreshToken
import io.aetera.model.auth.RefreshTokenId
import io.aetera.model.auth.RefreshTokenPolicy
import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.user.User
import io.aetera.usecase.user.UserDto
import org.springframework.stereotype.Component
import java.time.Clock

/**
 * 액세스 토큰 + 리프레시 토큰 한 쌍을 발급한다. 가입/로그인/재발급이 모두 이 경로를 쓴다.
 */
@Component
class SessionIssuer(
    private val accessTokenProvider: AccessTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val clock: Clock,
) {
    fun issue(user: User): AuthSessionDto {
        val access = accessTokenProvider.issue(user.id)
        val rawRefreshToken = OpaqueToken.generate()
        refreshTokenRepository.save(
            RefreshToken.issue(
                id = RefreshTokenId.next(),
                userId = user.id,
                tokenHash = OpaqueToken.hash(rawRefreshToken),
                issuedAt = clock.instant(),
                timeToLive = RefreshTokenPolicy.TIME_TO_LIVE,
            ),
        )
        return AuthSessionDto(
            accessToken = access.token,
            accessTokenExpiresInSeconds = access.expiresInSeconds,
            refreshToken = rawRefreshToken,
            user = UserDto(user),
        )
    }
}
