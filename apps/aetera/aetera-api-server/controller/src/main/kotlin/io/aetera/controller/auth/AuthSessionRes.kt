package io.aetera.controller.auth

import io.aetera.usecase.auth.AuthSessionDto
import io.aetera.usecase.user.UserDto

/**
 * [AuthSessionDto] 에서 리프레시 토큰 원문을 뺀 응답. 원문은 본문이 아니라
 * httpOnly 쿠키([RefreshTokenCookie])로만 나간다 — XSS 로 훔칠 수 있는 곳에 두지 않는다.
 */
data class AuthSessionRes(
    val accessToken: String,
    val accessTokenExpiresInSeconds: Long,
    val user: UserDto,
) {
    constructor(session: AuthSessionDto) : this(
        accessToken = session.accessToken,
        accessTokenExpiresInSeconds = session.accessTokenExpiresInSeconds,
        user = session.user,
    )
}
