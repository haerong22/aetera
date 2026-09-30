package io.aetera.controller.common

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Clock

/**
 * 아이피별 호출 한도.
 *
 * 인증 없이 열려 있는 길만 막는다 — 로그인·가입·비밀번호 찾기. 여기가 남의 계정을
 * 두드리거나 남의 메일함을 채우는 데 쓰이는 자리다. 로그인한 뒤의 API 는 이미 토큰이
 * 있어야 하므로 아이피로 나눌 이유가 적다.
 *
 * **막는 것은 한 아이피가 쏟아붓는 경우까지다.** 여러 아이피에 나눠 때리는 것은 이걸로
 * 못 막는다. 한 사람의 메일함이 표적일 때가 그런 경우라, 그쪽은 주소별로 따로 막는다
 * ([AuthController][io.aetera.controller.auth.AuthController]).
 *
 * 아이피는 [HttpServletRequest.getRemoteAddr] 에서 읽는다. 프록시 뒤에서는 이 값이
 * 프록시의 주소라 **모두가 한 통에 묶인다** — 운영은 `server.forward-headers-strategy`
 * 로 실제 주소를 복원한다(application-prod.yml). 그 설정을 믿을 수 있는 것은 신뢰하는
 * 로드밸런서 뒤에 있을 때뿐이라, 로컬에는 켜지 않는다.
 */
@Component
class RateLimitInterceptor(
    private val properties: RateLimitProperties,
    clock: Clock,
) : HandlerInterceptor {
    /** 경로마다 다른 한도. 숫자는 [RateLimitProperties] 가 들고 있다. */
    private val rules =
        listOf(
            Rule("POST", "/api/v1/auth/login", properties.login.limiter(clock)),
            Rule("POST", "/api/v1/auth/signup", properties.signup.limiter(clock)),
            Rule("POST", "/api/v1/auth/password-reset", properties.passwordReset.limiter(clock)),
            Rule(
                "POST",
                "/api/v1/auth/password-reset/confirm",
                properties.passwordResetConfirm.limiter(clock),
            ),
        )

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (!properties.enabled) return true

        // CORS 사전 요청은 실제 호출이 아니다. 여기서 한도를 먹으면 본 요청이 튕긴다.
        if (HttpMethod.OPTIONS.matches(request.method)) return true

        val rule = rules.firstOrNull { it.matches(request) } ?: return true
        val key = request.remoteAddr ?: UNKNOWN_CLIENT

        if (!rule.limiter.tryConsume(key)) {
            throw RateLimitedException(rule.limiter.retryAfter(key))
        }
        return true
    }

    private class Rule(
        val method: String,
        val path: String,
        val limiter: RateLimiter,
    ) {
        /**
         * 경로를 **정확히** 맞춘다. 접두사로 맞추면 `/password-reset` 규칙이
         * `/password-reset/confirm` 까지 먹어 둘이 한 통을 나눠 쓰게 된다 —
         * 링크를 받으려다 한도를 써 버린 사람이 링크를 못 쓰는 일이 생긴다.
         */
        fun matches(request: HttpServletRequest): Boolean = request.method == method && request.requestURI == path
    }

    private companion object {
        /** 주소를 못 읽는 경우. 한 통에 묶이지만, 안 막는 것보다는 낫다. */
        const val UNKNOWN_CLIENT = "unknown"
    }
}
