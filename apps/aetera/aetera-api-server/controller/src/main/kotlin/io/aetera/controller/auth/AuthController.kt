package io.aetera.controller.auth

import io.aetera.controller.common.RateLimitProperties
import io.aetera.controller.common.RateLimitedException
import io.aetera.controller.common.RateLimiter
import io.aetera.model.auth.AuthErrorCode
import io.aetera.shared.error.CoreException
import io.aetera.usecase.auth.AuthSessionDto
import io.aetera.usecase.auth.LoginService
import io.aetera.usecase.auth.LogoutService
import io.aetera.usecase.auth.RefreshSessionService
import io.aetera.usecase.auth.RequestPasswordResetService
import io.aetera.usecase.auth.ResetPasswordService
import io.aetera.usecase.auth.SignUpService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Clock

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
class AuthController(
    private val signUpService: SignUpService,
    private val loginService: LoginService,
    private val refreshSessionService: RefreshSessionService,
    private val logoutService: LogoutService,
    private val requestPasswordResetService: RequestPasswordResetService,
    private val resetPasswordService: ResetPasswordService,
    @Value("\${aetera.auth.cookie-secure:false}") private val cookieSecure: Boolean,
    private val rateLimit: RateLimitProperties,
    clock: Clock,
) {
    /**
     * 한 메일 주소로 보낼 수 있는 재설정 메일의 한도.
     *
     * 기본은 몰아서 세 번 — 안 왔다고 여겨 다시 누르는 것은 흔한 일이고, 스팸함에 갔거나
     * 메일이 늦는 날도 있다. 그 뒤로는 천천히 차오르므로 쏟아붓기에는 못 쓴다.
     */
    private val mailPerAddress = rateLimit.passwordResetPerAddress.limiter(clock)

    /**
     * 한 계정에 시도할 수 있는 로그인 횟수.
     *
     * 인터셉터의 아이피 한도만으로는 유출된 비밀번호 목록을 한 계정에 들이붓는 것을
     * 못 막는다 — 아이피를 흩으면 그만이다. 노려지는 계정 쪽에서 세야 걸린다.
     */
    private val loginPerAccount = rateLimit.loginPerAccount.limiter(clock)

    @PostMapping("/signup")
    @Operation(summary = "회원 가입. 가입 즉시 로그인 세션을 발급한다.")
    fun signUp(
        @RequestBody req: SignUpReq,
    ): ResponseEntity<AuthSessionRes> = sessionResponse(signUpService.signUp(req.toCommand()), HttpStatus.CREATED)

    @PostMapping("/login")
    @Operation(summary = "이메일 로그인. 계정별로도 시도 횟수를 센다.")
    fun login(
        @RequestBody req: LoginReq,
    ): ResponseEntity<AuthSessionRes> {
        /*
         * 여기는 **거절한다** — 비밀번호 찾기처럼 조용히 넘길 수 없다. 로그인은 성공하면
         * 세션을 주는 길이라, 막았는데 성공처럼 답할 수가 없다.
         *
         * 429 가 가입 여부를 흘리지는 않는다. 있는 계정이든 없는 계정이든 **적어 낸 주소로**
         * 세기 때문에, 없는 주소를 두드려도 똑같이 429 가 돌아온다.
         */
        requireQuota(loginPerAccount, req.email)
        return sessionResponse(loginService.login(req.toCommand()))
    }

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 재발급. 리프레시 토큰 쿠키를 회전시킨다.")
    fun refresh(request: HttpServletRequest): ResponseEntity<AuthSessionRes> {
        val rawToken =
            RefreshTokenCookie.read(request) ?: throw CoreException(AuthErrorCode.INVALID_REFRESH_TOKEN)
        return sessionResponse(refreshSessionService.refresh(rawToken))
    }

    /**
     * 비밀번호 재설정 요청.
     *
     * **가입 여부와 상관없이 항상 204 다.** 구분해서 답하면 이 주소가 가입 여부 조회기가 된다 —
     * 주소록을 넣고 돌리면 어느 주소가 이 서비스를 쓰는지 가려낼 수 있다.
     */
    @PostMapping("/password-reset")
    @Operation(summary = "비밀번호 재설정 메일 요청. 가입 여부를 알려 주지 않으려고 언제나 204 다.")
    fun requestPasswordReset(
        @RequestBody req: RequestPasswordResetReq,
    ): ResponseEntity<Void> {
        /*
         * **주소별로도 막는다.** 아이피 한도는 한 대가 쏟아붓는 것까지만 잡는데, 한 사람의
         * 메일함을 표적으로 삼으면 아이피를 바꿔 가며 때리면 그만이다. 받는 쪽에서 세어야
         * 그게 막힌다.
         *
         * 넘으면 **조용히 보내지 않는다.** 429 로 답하면 "한도가 찼다 = 아까 진짜로 보냈다"가
         * 되어 가입 여부가 새어 나간다 — 이 길이 통째로 막으려던 그것이다. 대소문자는 맞춰
         * 센다. 안 그러면 한 글자만 바꿔 같은 메일함을 계속 때릴 수 있다.
         */
        if (hasQuota(mailPerAddress, req.email)) {
            requestPasswordResetService.request(req.email)
        }
        return ResponseEntity.noContent().build()
    }

    /**
     * 링크로 받은 토큰으로 비밀번호를 다시 정한다.
     *
     * 세션을 주지 않는다 — 여기까지 온 사람이 정말 주인인지 아는 것은 **메일함을 열었다**는
     * 사실뿐이다. 그대로 로그인시키는 대신 로그인 화면으로 보내 새 비밀번호를 한 번 쓰게 한다.
     * 방금 정한 것을 손으로 넣어 보는 것이 오타를 마지막으로 거르는 자리이기도 하다.
     */
    @PostMapping("/password-reset/confirm")
    @Operation(summary = "비밀번호 재설정. 모든 세션이 끊기고 로그인은 새로 해야 한다.")
    fun resetPassword(
        @RequestBody req: ResetPasswordReq,
    ): ResponseEntity<Void> {
        resetPasswordService.reset(req.toCommand())
        return ResponseEntity
            .noContent()
            .header(HttpHeaders.SET_COOKIE, RefreshTokenCookie.expire(cookieSecure).toString())
            .build()
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃. 리프레시 토큰을 폐기하고 쿠키를 지운다.")
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        logoutService.logout(RefreshTokenCookie.read(request))
        return ResponseEntity
            .noContent()
            .header(HttpHeaders.SET_COOKIE, RefreshTokenCookie.expire(cookieSecure).toString())
            .build()
    }

    /** 넘었으면 거절한다. 세션을 주는 길처럼 "조용히 넘기기"가 불가능한 곳이 쓴다. */
    private fun requireQuota(
        limiter: RateLimiter,
        email: String,
    ) {
        if (!rateLimit.enabled) return
        val key = keyOf(email)
        if (!limiter.tryConsume(key)) throw RateLimitedException(limiter.retryAfter(key))
    }

    /** 넘었으면 false. 거절하면 그 자체가 정보가 되는 곳이 쓴다. */
    private fun hasQuota(
        limiter: RateLimiter,
        email: String,
    ): Boolean = !rateLimit.enabled || limiter.tryConsume(keyOf(email))

    /**
     * 대소문자와 공백을 맞춰 센다. 안 맞추면 한 글자만 바꿔 같은 계정·같은 메일함을
     * 계속 때릴 수 있어 한도가 있으나 마나가 된다.
     */
    private fun keyOf(email: String): String = email.trim().lowercase()

    private fun sessionResponse(
        session: AuthSessionDto,
        status: HttpStatus = HttpStatus.OK,
    ): ResponseEntity<AuthSessionRes> = ResponseEntity
        .status(status)
        .header(HttpHeaders.SET_COOKIE, RefreshTokenCookie.issue(session.refreshToken, cookieSecure).toString())
        .body(AuthSessionRes(session))
}
