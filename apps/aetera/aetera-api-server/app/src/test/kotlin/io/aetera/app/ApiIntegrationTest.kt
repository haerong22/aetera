package io.aetera.app

import com.jayway.jsonpath.JsonPath
import io.aetera.controller.common.HttpErrorMapper
import io.aetera.model.auth.AuthErrorCode
import io.aetera.model.module.ModuleErrorCode
import io.aetera.model.user.UserErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.transaction.annotation.Transactional

/**
 * 플랫폼 계약 전체를 한 흐름으로 검증한다:
 * 가입 → 인증 → 모듈 가드(403) → 모듈 활성화 → 모듈 API 사용 → 비활성화 → 다시 가드(403).
 */
@Tag("integration")
@SpringBootTest(properties = ["security.password.iterations=1000"])
@AutoConfigureMockMvc
@Import(TestcontainersConfig::class)
@Transactional
class ApiIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvcTester

    private fun signUp(email: String): Pair<String, String> {
        val response =
            mockMvc
                .post()
                .uri("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","nickname":"홍길동","password":"password1234"}""")
                .exchange()
        assertThat(response.response.status).isEqualTo(HttpStatus.CREATED.value())
        val accessToken = JsonPath.read<String>(response.response.contentAsString, "$.accessToken")
        val refreshCookie = requireNotNull(response.response.getCookie("aetera_rt")).value
        return accessToken to refreshCookie
    }

    private fun bearer(token: String): String = "Bearer $token"

    @Test
    fun `가입부터 모듈 사용까지 계약 전체가 한 흐름으로 동작한다`() {
        val (accessToken, _) = signUp("flow@example.com")

        // 1. 인증된 프로필 조회
        assertThat(
            mockMvc.get().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)),
        ).hasStatusOk().bodyJson().extractingPath("$.email").isEqualTo("flow@example.com")

        // 2. 모듈 스토어에는 일정 모듈이 보이지만 아직 비활성이다
        assertThat(
            mockMvc.get().uri("/api/v1/me/modules").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)),
        ).hasStatusOk().bodyJson().extractingPath("$[0].enabled").isEqualTo(false)

        // 3. 활성화 전에는 모듈 가드가 403 으로 자른다 — 모듈 쪽엔 검사 코드가 없다
        assertThat(
            mockMvc
                .get()
                .uri("/api/v1/modules/schedule/events?from=2026-03-01T00:00:00Z&to=2026-03-31T23:59:59Z")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)),
        ).hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .extractingPath("$.code")
            .isEqualTo(HttpErrorMapper.codeOf(ModuleErrorCode.MODULE_NOT_ENABLED))

        // 4. 모듈 활성화
        assertThat(
            mockMvc
                .post()
                .uri("/api/v1/me/modules/schedule/enablement")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)),
        ).hasStatusOk().bodyJson().extractingPath("$.enabled").isEqualTo(true)

        // 5. 일정 생성
        val created =
            mockMvc
                .post()
                .uri("/api/v1/modules/schedule/events")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "팀 회의",
                      "startsAt": "2026-03-05T09:00:00Z",
                      "endsAt": "2026-03-05T10:00:00Z",
                      "color": "#3182f6"
                    }
                    """.trimIndent(),
                ).exchange()
        assertThat(created.response.status).isEqualTo(HttpStatus.CREATED.value())

        // 6. 기간 조회에 잡힌다
        assertThat(
            mockMvc
                .get()
                .uri("/api/v1/modules/schedule/events?from=2026-03-01T00:00:00Z&to=2026-03-31T23:59:59Z")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)),
        ).hasStatusOk().bodyJson().extractingPath("$[0].title").isEqualTo("팀 회의")

        // 7. 비활성화하면 데이터는 남고 접근만 다시 막힌다
        assertThat(
            mockMvc
                .delete()
                .uri("/api/v1/me/modules/schedule/enablement")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)),
        ).hasStatusOk()

        assertThat(
            mockMvc
                .get()
                .uri("/api/v1/modules/schedule/events?from=2026-03-01T00:00:00Z&to=2026-03-31T23:59:59Z")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)),
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `토큰 없이 보호된 API 를 부르면 401 이다`() {
        assertThat(mockMvc.get().uri("/api/v1/me"))
            .hasStatus(HttpStatus.UNAUTHORIZED)
            .bodyJson()
            .extractingPath("$.code")
            .isEqualTo(HttpErrorMapper.codeOf(AuthErrorCode.UNAUTHENTICATED))
    }

    @Test
    fun `중복 가입은 409와 도메인 대역 코드로 거절한다`() {
        signUp("dup@example.com")

        assertThat(
            mockMvc
                .post()
                .uri("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"dup@example.com","nickname":"임꺽정","password":"password1234"}"""),
        ).hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .extractingPath("$.code")
            .isEqualTo(HttpErrorMapper.codeOf(UserErrorCode.EMAIL_ALREADY_REGISTERED))
    }

    @Test
    fun `틀린 비밀번호 로그인은 이메일 존재 여부를 노출하지 않는 단일 코드로 거절한다`() {
        signUp("secure@example.com")

        assertThat(
            mockMvc
                .post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"secure@example.com","password":"wrongpass1"}"""),
        ).hasStatus(HttpStatus.UNAUTHORIZED)
            .bodyJson()
            .extractingPath("$.code")
            .isEqualTo(HttpErrorMapper.codeOf(AuthErrorCode.LOGIN_FAILED))
    }

    @Test
    fun `리프레시 쿠키로 새 세션을 받으면 쿠키가 회전한다`() {
        val (_, refreshCookie) = signUp("refresh@example.com")

        val refreshed =
            mockMvc
                .post()
                .uri("/api/v1/auth/refresh")
                .cookie(jakarta.servlet.http.Cookie("aetera_rt", refreshCookie))
                .exchange()
        assertThat(refreshed.response.status).isEqualTo(HttpStatus.OK.value())
        val rotated = requireNotNull(refreshed.response.getCookie("aetera_rt")).value
        assertThat(rotated).isNotEqualTo(refreshCookie)

        // 회전한 토큰의 재사용 규칙(짧은 유예 vs 탈취 대응)은 커밋 경계까지 봐야 해서
        // RefreshTokenRotationIntegrationTest 가 따로 다룬다.
    }

    @Test
    fun `없는 모듈 활성화는 404 로 거절한다`() {
        val (accessToken, _) = signUp("nomodule@example.com")

        assertThat(
            mockMvc
                .post()
                .uri("/api/v1/me/modules/time-machine/enablement")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)),
        ).hasStatus(HttpStatus.NOT_FOUND)
            .bodyJson()
            .extractingPath("$.code")
            .isEqualTo(HttpErrorMapper.codeOf(ModuleErrorCode.MODULE_NOT_FOUND))
    }

    @Test
    fun `actuator health가 UP이다`() {
        assertThat(mockMvc.get().uri("/actuator/health"))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.status")
            .isEqualTo("UP")
    }
}
