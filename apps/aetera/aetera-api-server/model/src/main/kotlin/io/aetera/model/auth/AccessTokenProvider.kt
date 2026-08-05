package io.aetera.model.auth

import io.aetera.model.user.UserId

/**
 * 액세스 토큰 발급/검증 포트. 서명 알고리즘과 유효 기간 같은 상세는 infrastructure 가 갖는다.
 */
interface AccessTokenProvider {
    fun issue(userId: UserId): IssuedAccessToken

    /** 유효하지 않거나 만료된 토큰이면 null. */
    fun verify(token: String): UserId?
}

data class IssuedAccessToken(
    val token: String,
    val expiresInSeconds: Long,
)
