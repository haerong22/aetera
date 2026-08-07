package io.aetera.controller.auth

import io.aetera.model.auth.AuthErrorCode
import io.aetera.shared.error.CoreException
import io.aetera.usecase.auth.AuthSessionDto
import io.aetera.usecase.auth.LoginService
import io.aetera.usecase.auth.LogoutService
import io.aetera.usecase.auth.RefreshSessionService
import io.aetera.usecase.auth.SignUpService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
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
    @Value("\${aetera.auth.cookie-secure:false}") private val cookieSecure: Boolean,
) {
    @PostMapping("/signup")
    @Operation(summary = "회원 가입. 가입 즉시 로그인 세션을 발급한다.")
    fun signUp(
        @Valid @RequestBody req: SignUpReq,
    ): ResponseEntity<AuthSessionRes> = sessionResponse(signUpService.signUp(req.toCommand()), HttpStatus.CREATED)

    @PostMapping("/login")
    @Operation(summary = "이메일 로그인")
    fun login(
        @Valid @RequestBody req: LoginReq,
    ): ResponseEntity<AuthSessionRes> = sessionResponse(loginService.login(req.toCommand()))

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 재발급. 리프레시 토큰 쿠키를 회전시킨다.")
    fun refresh(request: HttpServletRequest): ResponseEntity<AuthSessionRes> {
        val rawToken =
            RefreshTokenCookie.read(request) ?: throw CoreException(AuthErrorCode.INVALID_REFRESH_TOKEN)
        return sessionResponse(refreshSessionService.refresh(rawToken))
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
