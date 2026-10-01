package io.aetera.app

import com.jayway.jsonpath.JsonPath
import io.aetera.model.user.Email
import io.aetera.model.user.UserRepository
import io.aetera.usecase.notification.BuildDigestService
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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.time.LocalDate
import java.util.UUID

/**
 * 변동지출을 실제 DB 로 확인한다.
 *
 * 단위 시험이 못 보는 것이 둘이다 — **같은 달을 두 번 적어도 줄이 하나인가**(유니크 인덱스가
 * 받치는 약속), 그리고 **끈 모듈의 API 가 막히는가**(코어 가드의 일).
 */
@Tag("integration")
@SpringBootTest(
    properties = [
        "security.password.iterations=1000",
        "aetera.rate-limit.enabled=false",
    ],
)
@AutoConfigureMockMvc
@Import(TestcontainersConfig::class)
class SpendingIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var buildDigestService: BuildDigestService

    @Autowired
    private lateinit var userRepository: UserRepository

    private val today = "2026-09-15"

    private fun signUp(email: String = "spending-${UUID.randomUUID()}@example.com"): String {
        val response =
            mockMvc
                .post()
                .uri("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","nickname":"홍길동","password":"password1234"}""")
                .exchange()
        assertThat(response.response.status).isEqualTo(HttpStatus.CREATED.value())
        return JsonPath.read(response.response.contentAsString, "$.accessToken")
    }

    private fun enableSpending(token: String) {
        val response =
            mockMvc
                .post()
                .uri("/api/v1/me/modules/spending/enablement")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .exchange()
        assertThat(response.response.status).isIn(HttpStatus.OK.value(), HttpStatus.CREATED.value())
    }

    private fun save(
        token: String,
        month: String,
        amount: Long,
        note: String? = null,
    ) = mockMvc
        .put()
        .uri("/api/v1/modules/spending/records/$month?today=$today")
        .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(if (note == null) """{"amount":$amount}""" else """{"amount":$amount,"note":"$note"}""")
        .exchange()

    private fun board(token: String) = mockMvc
        .get()
        .uri("/api/v1/modules/spending/records?today=$today")
        .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        .exchange()

    @Test
    fun `적으면 평균과 함께 돌려준다`() {
        val token = signUp()
        enableSpending(token)

        save(token, "2026-09-01", 600_000)
        val response = save(token, "2026-08-01", 400_000)

        val body = response.response.contentAsString
        assertThat(JsonPath.read<Int>(body, "$.monthlyAverage")).isEqualTo(500_000)
        assertThat(JsonPath.read<Int>(body, "$.averagedMonths")).isEqualTo(2)
        assertThat(JsonPath.read<List<*>>(body, "$.records")).hasSize(2)
    }

    /** 최근 달부터 와야 화면이 그대로 그린다. */
    @Test
    fun `최근 달이 위에 온다`() {
        val token = signUp()
        enableSpending(token)
        save(token, "2026-07-01", 100_000)
        save(token, "2026-09-01", 300_000)

        val body = board(token).response.contentAsString

        assertThat(JsonPath.read<String>(body, "$.records[0].month")).isEqualTo("2026-09-01")
    }

    /**
     * 명세서를 다시 보고 고치는 일이 흔하다. 줄이 쌓이면 합계와 평균이 전부 틀어진다 —
     * 유니크 인덱스가 받치는 약속이라 실제 DB 로만 확인된다.
     */
    @Test
    fun `같은 달을 두 번 적어도 줄은 하나다`() {
        val token = signUp()
        enableSpending(token)

        save(token, "2026-09-01", 300_000)
        val response = save(token, "2026-09-01", 520_000)

        val body = response.response.contentAsString
        assertThat(JsonPath.read<List<*>>(body, "$.records")).hasSize(1)
        assertThat(JsonPath.read<Int>(body, "$.records[0].amount")).isEqualTo(520_000)
    }

    @Test
    fun `며칠을 보내도 같은 달이면 같은 줄이다`() {
        val token = signUp()
        enableSpending(token)

        save(token, "2026-09-01", 300_000)
        val response = save(token, "2026-09-30", 520_000)

        assertThat(JsonPath.read<List<*>>(response.response.contentAsString, "$.records")).hasSize(1)
    }

    @Test
    fun `지우면 목록에서 빠진다`() {
        val token = signUp()
        enableSpending(token)
        save(token, "2026-09-01", 300_000)

        val response =
            mockMvc
                .delete()
                .uri("/api/v1/modules/spending/records/2026-09-01?today=$today")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .exchange()

        assertThat(JsonPath.read<List<*>>(response.response.contentAsString, "$.records")).isEmpty()
    }

    /** 조용히 성공으로 답하면 다른 사람의 달을 지우려 한 것도 성공이 된다. */
    @Test
    fun `없는 달을 지우면 404 다`() {
        val token = signUp()
        enableSpending(token)

        val response =
            mockMvc
                .delete()
                .uri("/api/v1/modules/spending/records/2026-01-01?today=$today")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .exchange()

        assertThat(response.response.status).isEqualTo(HttpStatus.NOT_FOUND.value())
    }

    /** 모듈을 켜지 않으면 코어 가드가 막는다. 이 모듈이 그 규약 안에 제대로 들어왔는지 본다. */
    @Test
    fun `켜지 않은 모듈의 API 는 막힌다`() {
        val token = signUp()

        assertThat(board(token).response.status).isEqualTo(HttpStatus.FORBIDDEN.value())
    }

    /** 아직 오지 않은 달은 쓴 돈이 있을 수 없다. */
    @Test
    fun `다음 달은 적을 수 없다`() {
        val token = signUp()
        enableSpending(token)

        assertThat(save(token, "2026-10-01", 100_000).response.status)
            .isEqualTo(HttpStatus.BAD_REQUEST.value())
    }

    /**
     * 내보내기에 이 모듈의 구역이 있어야 한다.
     *
     * `UserDataContributor` 를 구현하면 코어가 알아서 모아 가는데, **빠뜨리면 조용히
     * 빠진다** — 내 데이터를 받아 본 사람은 무엇이 없는지 모른다.
     */
    @Test
    fun `내보내기에 변동지출이 담긴다`() {
        val token = signUp()
        enableSpending(token)
        save(token, "2026-09-01", 620_000, note = "추석")

        val body =
            mockMvc
                .get()
                .uri("/api/v1/me/export")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .exchange()
                .response.contentAsString

        assertThat(JsonPath.read<Int>(body, "$.modules.spending[0].amount")).isEqualTo(620_000)
        assertThat(JsonPath.read<String>(body, "$.modules.spending[0].note")).isEqualTo("추석")
    }

    /**
     * 알림이 **다이제스트까지 닿는지** 본다.
     *
     * 기여자 단위 시험은 "줄을 만드는가"만 본다. 그 줄이 실제로 메일에 실리려면 빈이 등록되고
     * 켠 모듈 필터를 통과해야 하는데, 그건 스프링 배선이라 여기서만 드러난다 —
     * 기여자를 만들어 두고 `@Component` 를 빠뜨려도 단위 시험은 통과한다.
     */
    @Test
    fun `지난 달을 안 적었으면 다이제스트에 실린다`() {
        val email = "digest-${UUID.randomUUID()}@example.com"
        val token = signUp(email)
        enableSpending(token)
        val user = requireNotNull(userRepository.getByEmail(Email(email)))

        // 9월 초, 8월을 안 적은 상태.
        val digest = buildDigestService.build(user, LocalDate.of(2026, 9, 3))

        assertThat(digest).isNotNull
        assertThat(digest!!.notices.map { it.title }).anyMatch { it.contains("8월") }
    }

    /** 적어 둔 사람에게 보내는 알림은 재촉이 아니라 잡음이다. */
    @Test
    fun `적어 두면 다이제스트에 실리지 않는다`() {
        val email = "digest-${UUID.randomUUID()}@example.com"
        val token = signUp(email)
        enableSpending(token)
        save(token, "2026-08-01", 480_000)
        val user = requireNotNull(userRepository.getByEmail(Email(email)))

        val digest = buildDigestService.build(user, LocalDate.of(2026, 9, 3))

        // 변동지출 말고 켠 모듈이 없으므로 보낼 것이 아예 없어야 한다.
        assertThat(digest).isNull()
    }

    /** 탈퇴가 이 표도 지워야 한다 — 새 모듈이 생길 때마다 빠뜨리기 쉬운 자리다. */
    @Test
    fun `탈퇴하면 기록도 사라진다`() {
        val token = signUp()
        enableSpending(token)
        save(token, "2026-09-01", 300_000)
        val before = jdbcTemplate.queryForObject("select count(*) from spending_records", Long::class.java)!!
        assertThat(before).isPositive()

        mockMvc
            .delete()
            .uri("/api/v1/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()

        val after = jdbcTemplate.queryForObject("select count(*) from spending_records", Long::class.java)!!
        assertThat(after).isEqualTo(before - 1)
    }
}
