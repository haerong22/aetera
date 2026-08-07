package io.aetera.controller.common

import io.aetera.model.auth.AccessTokenProvider
import io.aetera.model.auth.AuthErrorCode
import io.aetera.shared.error.CoreException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * `Authorization: Bearer <액세스 토큰>` 을 검증하고 사용자 아이디를 요청에 싣는다.
 * 실패는 [CoreException] 으로 던져서 [GlobalExceptionHandler] 가 401 로 바꾼다.
 */
@Component
class AuthInterceptor(
    private val accessTokenProvider: AccessTokenProvider,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        // CORS 사전 요청은 인증 헤더가 없다.
        if (HttpMethod.OPTIONS.matches(request.method)) return true

        val token =
            request
                .getHeader(HttpHeaders.AUTHORIZATION)
                ?.takeIf { it.startsWith(BEARER_PREFIX) }
                ?.removePrefix(BEARER_PREFIX)
                ?: throw CoreException(AuthErrorCode.UNAUTHENTICATED)

        val userId = accessTokenProvider.verify(token) ?: throw CoreException(AuthErrorCode.UNAUTHENTICATED)
        request.setAttribute(USER_ID_ATTRIBUTE, userId.value)
        return true
    }

    companion object {
        const val USER_ID_ATTRIBUTE: String = "aetera.userId"
        private const val BEARER_PREFIX = "Bearer "
    }
}
