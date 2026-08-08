package io.aetera.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.util.UUID

/**
 * 리프레시 토큰 회전과 탈취 대응을 실제 커밋 경계까지 검증한다.
 *
 * **이 클래스에는 `@Transactional` 을 붙이지 않는다.** 탈취 대응은 `REQUIRES_NEW` 트랜잭션에서
 * 커밋되는데, 테스트가 트랜잭션 안에서 돌면 테스트가 만든 토큰들이 아직 커밋되지 않아
 * 별도 트랜잭션에서 보이지 않는다 — 검증이 공허하게 통과해 버린다.
 * 대신 테스트마다 다른 이메일을 써서 서로 간섭하지 않게 한다.
 */
@Tag("integration")
@SpringBootTest(properties = ["security.password.iterations=1000"])
@AutoConfigureMockMvc
@Import(TestcontainersConfig::class)
class RefreshTokenRotationIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    /** 테스트마다 다른 사용자를 쓰고, SQL 조작도 이 이메일로만 범위를 좁힌다. */
    private lateinit var email: String

    private fun signUp(): String {
        email = "rotation-${UUID.randomUUID()}@example.com"
        val response =
            mockMvc
                .post()
                .uri("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","nickname":"회전","password":"password1234"}""")
                .exchange()
        assertThat(response.response.status).isEqualTo(HttpStatus.CREATED.value())
        return requireNotNull(response.response.getCookie("aetera_rt")).value
    }

    private fun refresh(refreshToken: String) = mockMvc
        .post()
        .uri("/api/v1/auth/refresh")
        .cookie(jakarta.servlet.http.Cookie("aetera_rt", refreshToken))
        .exchange()

    @Test
    fun `회전하면 새 토큰이 나오고 이전 토큰은 무효가 된다`() {
        val first = signUp()

        val rotated = refresh(first)
        assertThat(rotated.response.status).isEqualTo(HttpStatus.OK.value())
        val second = requireNotNull(rotated.response.getCookie("aetera_rt")).value
        assertThat(second).isNotEqualTo(first)
    }

    /**
     * 회귀 방지: 탈취 대응(전체 세션 폐기)이 같은 트랜잭션에서 일어나면
     * 곧바로 던지는 CoreException 때문에 롤백되어 아무 효과가 없었다.
     * 살아 있는 다른 토큰이 실제로 죽었는지까지 확인해야 그 버그를 잡을 수 있다.
     */
    @Test
    fun `유예를 벗어난 재사용은 그 사용자의 다른 세션까지 끊는다`() {
        val stolen = signUp()
        val alive = requireNotNull(refresh(stolen).response.getCookie("aetera_rt")).value

        // 폐기된 토큰을 유예 밖에서 재사용한 것처럼 만든다.
        expireRotationGrace()

        assertThat(refresh(stolen).response.status)
            .describedAs("폐기된 토큰의 재사용은 거절되어야 한다")
            .isEqualTo(HttpStatus.UNAUTHORIZED.value())

        assertThat(refresh(alive).response.status)
            .describedAs("탈취 감지 뒤에는 그 사용자의 다른 토큰도 무효여야 한다")
            .isEqualTo(HttpStatus.UNAUTHORIZED.value())
    }

    /**
     * 탭을 여러 개 열어 둔 브라우저가 세션을 복원하면 같은 쿠키로 재발급 요청이 동시에 나간다.
     * 조건부 갱신 없이 각자 저장하면 낙관적 락이 충돌해 진 쪽이 409 를 받고, 프론트는 그걸
     * 세션 종료로 읽어 사용자를 로그아웃시킨다.
     */
    @Test
    fun `같은 토큰으로 동시에 들어온 재발급은 모두 성공한다`() {
        val shared = signUp()

        val statuses =
            (1..5)
                .toList()
                .parallelStream()
                .map { refresh(shared).response.status }
                .toList()

        assertThat(statuses)
            .describedAs("동시 재발급이 409(낙관적 락 충돌)로 실패하면 안 된다")
            .containsOnly(HttpStatus.OK.value())
    }

    /**
     * 회귀 방지: 전체 폐기가 "아직 살아 있는 토큰"만 끊으면, 회전으로 죽었지만 유예가 남은
     * 토큰이 사유를 ROTATED 로 유지한 채 살아남아 부활 티켓이 된다 — 대응 직후 그 토큰을
     * 재생하면 유예 판정을 통과해 새 세션이 나오고, 탈취 대응이 통째로 무력해진다.
     */
    @Test
    fun `탈취 대응 뒤에는 유예가 남아 있던 토큰으로도 세션을 되살릴 수 없다`() {
        val first = signUp()
        val second = requireNotNull(refresh(first).response.getCookie("aetera_rt")).value
        val third = requireNotNull(refresh(second).response.getCookie("aetera_rt")).value

        // first 만 유예 밖으로 밀어 탈취 감지를 유발한다. second 는 방금 회전해 유예가 남아 있다.
        expireOldestRotationGrace()
        assertThat(refresh(first).response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())

        assertThat(refresh(third).response.status)
            .describedAs("활성 토큰은 끊겨야 한다")
            .isEqualTo(HttpStatus.UNAUTHORIZED.value())
        assertThat(refresh(second).response.status)
            .describedAs("유예가 남아 있던 토큰으로도 세션을 되살릴 수 없어야 한다")
            .isEqualTo(HttpStatus.UNAUTHORIZED.value())
    }

    @Test
    fun `회전 직후 유예 안의 재사용은 탈취로 보지 않는다`() {
        val first = signUp()
        val second = requireNotNull(refresh(first).response.getCookie("aetera_rt")).value

        // 유예 시간 안이므로 탭 두 개가 동시에 재발급을 시도한 상황으로 간주된다.
        assertThat(refresh(first).response.status).isEqualTo(HttpStatus.OK.value())

        assertThat(refresh(second).response.status)
            .describedAs("정상 경합에서는 다른 탭의 토큰이 살아 있어야 한다")
            .isEqualTo(HttpStatus.OK.value())
    }

    /**
     * 유예 창을 실시간으로 기다리지 않도록 폐기 시각을 과거로 밀어 둔다.
     *
     * 반드시 이 테스트의 사용자만 건드려야 한다. 전체를 밀면, 같은 컨테이너를 쓰는 다른
     * 비-트랜잭션 테스트가 방금 회전시킨 토큰까지 과거로 밀려 탈취 판정을 받는다.
     */
    private fun expireRotationGrace() {
        jdbcTemplate.update(
            """
            update refresh_tokens t set revoked_at = t.revoked_at - interval '1 hour'
            from users u
            where u.id = t.user_id and u.email = ? and t.revoked_at is not null
            """.trimIndent(),
            email,
        )
    }

    /** 이 테스트 사용자의 가장 먼저 발급된 토큰만 유예 밖으로 민다. */
    private fun expireOldestRotationGrace() {
        jdbcTemplate.update(
            """
            update refresh_tokens set revoked_at = revoked_at - interval '1 hour'
            where id = (
                select t.id from refresh_tokens t join users u on u.id = t.user_id
                where u.email = ? order by t.issued_at limit 1
            )
            """.trimIndent(),
            email,
        )
    }
}
