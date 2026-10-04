package io.aetera.app

import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.util.UUID

/**
 * 비밀번호 찾기를 **메일에 실린 링크 그대로** 끝까지 돌려 본다.
 *
 * 토큰이 원문으로 나가고 해시로 저장되므로, 서비스 단위 시험만으로는 그 둘이 실제 DB 를
 * 사이에 두고 맞는지 알 수 없다. 여기서는 발송기가 받은 본문에서 링크를 뽑아 그대로 보낸다 —
 * 사용자가 하는 것과 같은 일이다.
 *
 * `@Transactional` 을 붙이지 않는다. 요청 사이에 실제로 커밋돼 있어야 다음 요청이 본다.
 */
@AeteraIntegrationTest
class PasswordResetIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var outbox: Outbox

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val password = "password1234"
    private val newPassword = "newpassword5678"

    /** 컨텍스트를 돌려 쓰므로 앞 클래스가 남긴 메일이 보인다 — 읽는 쪽이 비운다. */
    @BeforeEach
    fun clearOutbox() {
        outbox.reset()
    }

    private fun newEmail() = "reset-${UUID.randomUUID()}@example.com"

    private fun signUp(email: String): String {
        val response =
            mockMvc
                .post()
                .uri("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","nickname":"홍길동","password":"$password"}""")
                .exchange()
        assertThat(response.response.status).isEqualTo(HttpStatus.CREATED.value())
        return requireNotNull(response.response.getCookie("aetera_rt")).value
    }

    private fun requestReset(email: String) = mockMvc
        .post()
        .uri("/api/v1/auth/password-reset")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"email":"$email"}""")
        .exchange()

    private fun confirm(
        token: String,
        next: String = newPassword,
    ) = mockMvc
        .post()
        .uri("/api/v1/auth/password-reset/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"token":"$token","newPassword":"$next"}""")
        .exchange()

    private fun login(
        email: String,
        withPassword: String,
    ) = mockMvc
        .post()
        .uri("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"email":"$email","password":"$withPassword"}""")
        .exchange()

    private fun refresh(refreshToken: String) = mockMvc
        .post()
        .uri("/api/v1/auth/refresh")
        .cookie(jakarta.servlet.http.Cookie("aetera_rt", refreshToken))
        .exchange()

    /** 메일 본문에서 사용자가 누를 링크의 토큰을 뽑는다. */
    private fun tokenFromMail(): String = outbox.resetTokenFromLastMail()

    @Test
    fun `메일로 온 링크로 비밀번호를 다시 정한다`() {
        val email = newEmail()
        signUp(email)

        assertThat(requestReset(email).response.status).isEqualTo(HttpStatus.NO_CONTENT.value())
        assertThat(confirm(tokenFromMail()).response.status).isEqualTo(HttpStatus.NO_CONTENT.value())

        assertThat(login(email, newPassword).response.status).isEqualTo(HttpStatus.OK.value())
        assertThat(login(email, password).response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
    }

    /**
     * 로그인하지 않고 부를 수 있어야 한다. 인터셉터 제외 목록에서 빠지면 401 이 오고,
     * **못 들어오는 사람을 위한 기능이 로그인해야 쓸 수 있게** 된다.
     */
    @Test
    fun `로그인 없이 요청할 수 있다`() {
        val email = newEmail()
        signUp(email)

        val response = requestReset(email)

        assertThat(response.response.status).isEqualTo(HttpStatus.NO_CONTENT.value())
        assertThat(outbox.sent).hasSize(1)
    }

    /**
     * 가입 여부가 응답으로 새면 이 주소가 가입 여부 조회기가 된다.
     * 상태 코드도 본문도 같아야 한다 — 메일이 나가는지만 다르다.
     */
    @Test
    fun `없는 주소도 똑같이 답한다`() {
        val existing = newEmail()
        signUp(existing)
        val known = requestReset(existing)
        outbox.sent.clear()

        val unknown = requestReset("nobody-${UUID.randomUUID()}@example.com")

        assertThat(unknown.response.status).isEqualTo(known.response.status)
        assertThat(unknown.response.contentAsString).isEqualTo(known.response.contentAsString)
        assertThat(outbox.sent).isEmpty()
    }

    /** 메일은 지워지지 않고 남는다. 다 쓴 링크가 계속 통하면 그 메일을 나중에 본 사람이 들어간다. */
    @Test
    fun `같은 링크를 두 번 쓸 수 없다`() {
        val email = newEmail()
        signUp(email)
        requestReset(email)
        val token = tokenFromMail()

        confirm(token)

        assertThat(confirm(token, "another5678pw").response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
        // 두 번째가 통했다면 비밀번호가 또 바뀌었을 것이다.
        assertThat(login(email, newPassword).response.status).isEqualTo(HttpStatus.OK.value())
    }

    /** 여러 번 요청하면 링크가 여러 통 나간다. 하나를 쓰면 나머지도 죽어야 한다. */
    @Test
    fun `하나를 쓰면 먼저 보낸 링크도 죽는다`() {
        val email = newEmail()
        signUp(email)

        requestReset(email)
        val first = tokenFromMail()
        requestReset(email)
        val second = tokenFromMail()
        assertThat(first).isNotEqualTo(second)

        confirm(second)

        assertThat(confirm(first, "another5678pw").response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
    }

    /**
     * 비밀번호를 잊은 것이 아니라 **빼앗긴** 것일 수 있다. 세션을 남겨 두면
     * 비밀번호만 바뀌고 침입자는 안에 그대로 있다.
     */
    @Test
    fun `다시 정하면 열려 있던 세션이 모두 끊긴다`() {
        val email = newEmail()
        val openSession = signUp(email)
        assertThat(refresh(openSession).response.status).isEqualTo(HttpStatus.OK.value())

        requestReset(email)
        confirm(tokenFromMail())

        assertThat(refresh(openSession).response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
    }

    /**
     * 메일 서버가 흔들리는 동안 이 주소가 **가입 여부 조회기**로 바뀌면 안 된다.
     * 가입한 주소만 500 이 되면 없는 주소(204)와의 차이로 가려낼 수 있다 —
     * 이 기능이 통째로 막으려던 그것이다.
     */
    @Test
    fun `발송이 실패해도 없는 주소와 같은 응답이다`() {
        val email = newEmail()
        signUp(email)
        outbox.broken = true

        val withAccount = requestReset(email)
        val withoutAccount = requestReset("nobody-${UUID.randomUUID()}@example.com")

        assertThat(withAccount.response.status).isEqualTo(HttpStatus.NO_CONTENT.value())
        assertThat(withAccount.response.status).isEqualTo(withoutAccount.response.status)
        assertThat(withAccount.response.contentAsString).isEqualTo(withoutAccount.response.contentAsString)
    }

    @Test
    fun `아무 토큰이나 넣으면 거절한다`() {
        assertThat(confirm("아무거나").response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
    }

    /** 만료를 시계 대신 DB 로 만든다 — 30분을 실제로 기다릴 수는 없다. */
    @Test
    fun `만료된 링크는 통하지 않는다`() {
        val email = newEmail()
        signUp(email)
        requestReset(email)
        val token = tokenFromMail()

        jdbcTemplate.update(
            "update password_reset_tokens set expires_at = now() - interval '1 minute' " +
                "where user_id = (select id from users where email = ?)",
            email,
        )

        assertThat(confirm(token).response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
        assertThat(login(email, password).response.status).isEqualTo(HttpStatus.OK.value())
    }

    /**
     * 원문이 저장돼 있으면 DB 한 벌이 곧 전 계정의 마스터키가 된다.
     * 메일에 실린 값이 표에 그대로 있지 않은지 직접 본다.
     */
    @Test
    fun `보낸 토큰이 표에 그대로 저장되지 않는다`() {
        val email = newEmail()
        signUp(email)
        requestReset(email)
        val token = tokenFromMail()

        val stored =
            jdbcTemplate.queryForObject(
                "select token_hash from password_reset_tokens " +
                    "where user_id = (select id from users where email = ?)",
                String::class.java,
                email,
            )

        assertThat(stored).isNotEqualTo(token)
    }
}
