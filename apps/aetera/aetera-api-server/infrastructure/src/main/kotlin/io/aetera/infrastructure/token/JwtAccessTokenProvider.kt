package io.aetera.infrastructure.token

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.aetera.model.auth.AccessTokenProvider
import io.aetera.model.auth.IssuedAccessToken
import io.aetera.model.user.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.util.Date

/**
 * HS256 서명 JWT 액세스 토큰. 짧게 살고(기본 15분) 상태를 저장하지 않는다 —
 * 만료 전 강제 무효화가 필요하면 리프레시 토큰을 폐기해 다음 재발급을 막는 방식으로 처리한다.
 */
@Component
class JwtAccessTokenProvider(
    @Value("\${aetera.auth.jwt-secret}") secret: String,
    @Value("\${aetera.auth.access-token-ttl:15m}") private val timeToLive: Duration,
    private val clock: Clock,
) : AccessTokenProvider {
    private val secretBytes = secret.toByteArray(Charsets.UTF_8)

    // 둘 다 스레드 안전하고 키만 들고 있다. 매 요청 새로 만들면 인증 경로에서 가장 뜨거운
    // 지점(모든 인증 요청의 verify)에 키 검증과 할당이 반복된다.
    private val signer by lazy { MACSigner(secretBytes) }
    private val verifier by lazy { MACVerifier(secretBytes) }

    init {
        // HS256 은 256bit 이상 키를 요구한다. 짧은 키로 조용히 돌게 두면 안 된다.
        require(secretBytes.size >= MIN_SECRET_BYTES) {
            "aetera.auth.jwt-secret 은 ${MIN_SECRET_BYTES}바이트 이상이어야 합니다. 현재: ${secretBytes.size}바이트"
        }
    }

    override fun issue(userId: UserId): IssuedAccessToken {
        val now = clock.instant()
        val claims =
            JWTClaimsSet
                .Builder()
                .subject(userId.value.toString())
                .issuer(ISSUER)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(timeToLive)))
                .build()
        val jwt = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claims)
        jwt.sign(signer)
        return IssuedAccessToken(token = jwt.serialize(), expiresInSeconds = timeToLive.seconds)
    }

    override fun verify(token: String): UserId? = runCatching {
        val jwt = SignedJWT.parse(token)
        if (!jwt.verify(verifier)) return null

        val claims = jwt.jwtClaimsSet
        if (claims.issuer != ISSUER) return null
        val expiresAt = claims.expirationTime ?: return null
        if (!expiresAt.toInstant().isAfter(clock.instant())) return null

        UserId.of(claims.subject ?: return null)
    }.getOrNull()

    companion object {
        private const val ISSUER = "aetera"
        private const val MIN_SECRET_BYTES = 32
    }
}
