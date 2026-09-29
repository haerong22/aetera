package io.aetera.controller.auth

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
) {
    @PostMapping("/signup")
    @Operation(summary = "회원 가입. 가입 즉시 로그인 세션을 발급한다.")
    fun signUp(
        @RequestBody req: SignUpReq,
    ): ResponseEntity<AuthSessionRes> = sessionResponse(signUpService.signUp(req.toCommand()), HttpStatus.CREATED)

    @PostMapping("/login")
    @Operation(summary = "이메일 로그인")
    fun login(
        @RequestBody req: LoginReq,
    ): ResponseEntity<AuthSessionRes> = sessionResponse(loginService.login(req.toCommand()))

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
        requestPasswordResetService.request(req.email)
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

    private fun sessionResponse(
        session: AuthSessionDto,
        status: HttpStatus = HttpStatus.OK,
    ): ResponseEntity<AuthSessionRes> = ResponseEntity
        .status(status)
        .header(HttpHeaders.SET_COOKIE, RefreshTokenCookie.issue(session.refreshToken, cookieSecure).toString())
        .body(AuthSessionRes(session))
}
