package io.aetera.app

import com.jayway.jsonpath.JsonPath
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
import java.util.UUID

/**
 * 비밀번호 변경은 **두 기기가 있어야 의미가 드러난다.**
 *
 * 한 기기로만 보면 "바뀌었다"밖에 확인할 수 없는데, 이 기능의 목적은 그게 아니다.
 * 남이 이미 로그인해 두었을 때 **그 세션이 끊기는가**가 전부다. 그래서 여기서는
 * 기기 둘을 만들어 하나로 바꾸고 다른 하나가 죽는지 본다.
 *
 * `@Transactional` 을 붙이지 않는다 — 세션 폐기와 발급이 실제 커밋 경계를 넘어가야
 * 다른 요청에서 보인다.
 */
@Tag("integration")
/*
 * 호출 한도를 끈다. 이 시험들은 전부 같은 주소에서 수십 번 부르므로 켜 두면
 * 한도에 걸려 무너진다 — 여기서 볼 것은 한도가 아니다.
 * 한도 자체는 [RateLimitIntegrationTest] 가 본다.
 */
@SpringBootTest(
    properties = [
        "security.password.iterations=1000",
        "aetera.rate-limit.enabled=false",
    ],
)
@AutoConfigureMockMvc
@Import(TestcontainersConfig::class)
class ChangePasswordIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvcTester

    private val password = "password1234"
    private val newPassword = "newpassword5678"

    /** 기기 하나. 액세스 토큰과 리프레시 쿠키를 함께 들고 있다. */
    private data class Device(
        val accessToken: String,
        val refreshToken: String,
    )

    private fun signUp(email: String): Device {
        val response =
            mockMvc
                .post()
                .uri("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","nickname":"홍길동","password":"$password"}""")
                .exchange()
        assertThat(response.response.status).isEqualTo(HttpStatus.CREATED.value())
        return Device(
            accessToken = JsonPath.read(response.response.contentAsString, "$.accessToken"),
            refreshToken = requireNotNull(response.response.getCookie("aetera_rt")).value,
        )
    }

    /** 같은 계정으로 한 번 더 로그인한다 — 다른 기기에서 들어온 셈이다. */
    private fun login(
        email: String,
        withPassword: String = password,
    ): Device {
        val response =
            mockMvc
                .post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","password":"$withPassword"}""")
                .exchange()
        assertThat(response.response.status).isEqualTo(HttpStatus.OK.value())
        return Device(
            accessToken = JsonPath.read(response.response.contentAsString, "$.accessToken"),
            refreshToken = requireNotNull(response.response.getCookie("aetera_rt")).value,
        )
    }

    private fun refresh(refreshToken: String) = mockMvc
        .post()
        .uri("/api/v1/auth/refresh")
        .cookie(jakarta.servlet.http.Cookie("aetera_rt", refreshToken))
        .exchange()

    private fun changePassword(
        device: Device,
        current: String = password,
        next: String = newPassword,
    ) = mockMvc
        .put()
        .uri("/api/v1/me/password")
        .header(HttpHeaders.AUTHORIZATION, "Bearer ${device.accessToken}")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"currentPassword":"$current","newPassword":"$next"}""")
        .exchange()

    private fun newEmail() = "password-${UUID.randomUUID()}@example.com"

    @Test
    fun `바꾼 비밀번호로 로그인된다`() {
        val email = newEmail()
        val device = signUp(email)

        assertThat(changePassword(device).response.status).isEqualTo(HttpStatus.NO_CONTENT.value())

        login(email, newPassword)
    }

    @Test
    fun `옛 비밀번호로는 로그인되지 않는다`() {
        val email = newEmail()
        val device = signUp(email)

        changePassword(device)

        val response =
            mockMvc
                .post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","password":"$password"}""")
                .exchange()
        assertThat(response.response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
    }

    /**
     * 이 시험이 이 기능의 이유다.
     *
     * 리프레시 토큰은 비밀번호와 무관하게 살아 있으므로, 끊지 않으면 남이 열어 둔 세션이
     * 그대로 유지된다. 잠금장치를 바꾸고 침입자를 안에 두는 셈인데 화면은 성공이라고 말한다.
     */
    @Test
    fun `바꾸면 다른 기기의 세션이 끊긴다`() {
        val email = newEmail()
        val mine = signUp(email)
        val other = login(email)

        // 바꾸기 전에는 살아 있다. 이걸 먼저 보지 않으면 "원래 죽어 있었다"와 구분되지 않는다.
        assertThat(refresh(other.refreshToken).response.status).isEqualTo(HttpStatus.OK.value())

        changePassword(mine)

        assertThat(refresh(other.refreshToken).response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
    }

    /**
     * 끊기만 하고 새로 주지 않으면 바꾼 사람도 함께 튕긴다. 발급이 폐기보다 먼저 일어나면
     * 방금 만든 토큰까지 같이 끊기는데, 증상이 똑같아서 순서를 의심하기 어렵다.
     */
    @Test
    fun `바꾼 기기는 새 쿠키를 받아 계속 쓴다`() {
        val email = newEmail()
        val mine = signUp(email)

        val response = changePassword(mine)
        val issued = requireNotNull(response.response.getCookie("aetera_rt")).value

        assertThat(issued).isNotEqualTo(mine.refreshToken)
        assertThat(refresh(issued).response.status).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun `바꾼 기기의 옛 쿠키는 더 이상 통하지 않는다`() {
        val email = newEmail()
        val mine = signUp(email)

        changePassword(mine)

        assertThat(refresh(mine.refreshToken).response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
    }

    @Test
    fun `지금 비밀번호가 틀리면 아무것도 바뀌지 않는다`() {
        val email = newEmail()
        val mine = signUp(email)
        val other = login(email)

        val response = changePassword(mine, current = "wrongpassword1")
        assertThat(response.response.status).isEqualTo(HttpStatus.BAD_REQUEST.value())

        // 비밀번호도, 남의 세션도 그대로여야 한다 — 실패한 요청이 절반만 실행되면 안 된다.
        login(email, password)
        assertThat(refresh(other.refreshToken).response.status).isEqualTo(HttpStatus.OK.value())
    }
}
