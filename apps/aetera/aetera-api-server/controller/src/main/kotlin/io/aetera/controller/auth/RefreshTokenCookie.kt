package io.aetera.controller.auth

import io.aetera.model.auth.RefreshTokenPolicy
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseCookie
import java.time.Duration

/**
 * 리프레시 토큰을 담는 httpOnly 쿠키. JS 가 읽을 수 없고,
 * `/api/v1/auth` 경로에만 실려 다른 API 요청에는 아예 전송되지 않는다.
 */
object RefreshTokenCookie {
    const val NAME: String = "aetera_rt"
    private const val PATH: String = "/api/v1/auth"

    fun issue(
        rawToken: String,
        secure: Boolean,
    ): ResponseCookie = base(rawToken, secure).maxAge(RefreshTokenPolicy.TIME_TO_LIVE).build()

    fun expire(secure: Boolean): ResponseCookie = base("", secure).maxAge(Duration.ZERO).build()

    fun read(request: HttpServletRequest): String? = request.cookies
        ?.firstOrNull { it.name == NAME }
        ?.value
        ?.takeIf { it.isNotBlank() }

    private fun base(
        value: String,
        secure: Boolean,
    ): ResponseCookie.ResponseCookieBuilder = ResponseCookie
        .from(NAME, value)
        .httpOnly(true)
        .secure(secure)
        .path(PATH)
        .sameSite("Lax")
}
