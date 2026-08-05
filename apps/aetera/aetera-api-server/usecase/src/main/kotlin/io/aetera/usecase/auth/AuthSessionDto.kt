package io.aetera.usecase.auth

import io.aetera.usecase.user.UserDto

/**
 * 로그인/재발급의 결과. [refreshToken] 원문은 여기서 인바운드 어댑터까지만 흘러가
 * httpOnly 쿠키로 나가고, 응답 본문에는 담지 않는다.
 */
data class AuthSessionDto(
    val accessToken: String,
    val accessTokenExpiresInSeconds: Long,
    val refreshToken: String,
    val user: UserDto,
)
