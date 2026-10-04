package io.aetera.app

import com.jayway.jsonpath.JsonPath
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester

/**
 * 탈퇴는 **진짜 DB 로 확인해야 한다.**
 *
 * 목으로는 "지우라고 불렀는가"만 볼 수 있는데, 실제로 걸린 문제는 그 층 아래였다 —
 * 파생 삭제가 지움 표시만 해 둔 사이에 다른 기여자의 `@Modifying(clearAutomatically)` 가
 * 영속성 컨텍스트를 비워 **그 표시가 버려졌다.** 목은 영속성 컨텍스트를 모르므로
 * 그 시험은 통과했을 것이다.
 *
 * `@Transactional` 을 붙이지 않는다. 붙이면 모든 것이 한 트랜잭션에 묶여
 * flush/clear 가 실제와 다르게 움직이고, 방금 그 버그를 놓친다.
 */
@AeteraIntegrationTest
class WithdrawIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var entityManager: EntityManager

    private fun bearer(token: String) = "Bearer $token"

    private fun signUp(email: String): String {
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

    private fun post(
        token: String,
        uri: String,
        body: String? = null,
    ) {
        val request = mockMvc.post().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(token))
        val response =
            if (body == null) {
                request.exchange()
            } else {
                request.contentType(MediaType.APPLICATION_JSON).content(body).exchange()
            }
        assertThat(response.response.status).isIn(HttpStatus.OK.value(), HttpStatus.CREATED.value())
    }

    private fun put(
        token: String,
        uri: String,
        body: String,
    ) {
        val response =
            mockMvc
                .put()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange()
        assertThat(response.response.status).isEqualTo(HttpStatus.OK.value())
    }

    private fun countFor(
        table: String,
        userId: String,
    ): Long = entityManager
        .createNativeQuery("select count(*) from $table where user_id = cast(:id as uuid)")
        .setParameter("id", userId)
        .singleResult as Long

    private fun userIdOf(email: String): String = entityManager
        .createNativeQuery("select cast(id as varchar) from users where email = :email")
        .setParameter("email", email)
        .singleResult as String

    /**
     * 데이터를 **일곱 모듈 전부에** 심는다.
     *
     * 하나만 심으면 놓친다 — 처음 버그를 만났을 때 자산만 있는 계정으로는 멀쩡히 지워졌고,
     * 일곱을 다 채운 계정에서만 셋이 남았다. 기여자가 여럿일 때만 드러나는 문제였다.
     */
    private fun seedEverything(
        token: String,
        email: String,
    ) {
        listOf("asset", "income", "expense", "goal", "renewal", "schedule", "resignation")
            .forEach { post(token, "/api/v1/me/modules/$it/enablement") }

        put(
            token,
            "/api/v1/modules/asset/snapshots/2026-09-01",
            """{"entries":[{"name":"통장","category":"CASH","amount":1000}]}""",
        )
        post(
            token,
            "/api/v1/modules/income/items",
            """{"title":"월급","category":"SALARY","amount":3000000,"cycle":"MONTHLY"}""",
        )
        post(
            token,
            "/api/v1/modules/expense/items",
            """{"title":"월세","category":"HOUSING","amount":700000,"cycle":"MONTHLY"}""",
        )
        post(token, "/api/v1/modules/goal/goals", """{"title":"운동","period":"WEEKLY","target":3}""")
        post(
            token,
            "/api/v1/modules/renewal/items",
            """{"title":"보험","expiresAt":"2027-01-01","cycle":"YEARLY"}""",
        )
        post(
            token,
            "/api/v1/modules/schedule/events",
            """{"title":"회의","startsAt":"2026-10-01T01:00:00Z","endsAt":"2026-10-01T02:00:00Z"}""",
        )
        put(token, "/api/v1/modules/resignation/guide/journey", """{"anchorDate":"2026-12-31"}""")
        put(
            token,
            "/api/v1/modules/resignation/guide/tasks/finance-runway",
            """{"done":true,"note":"확인"}""",
        )
        put(token, "/api/v1/me/notifications", """{"enabled":true,"sendHour":9}""")

        /*
         * 재설정 토큰도 한 줄 만들어 둔다. 없으면 "심기 전" 단언이 0 이라 헛통과하고,
         * 탈퇴가 이 표를 안 지워도 아무도 모른다.
         */
        mockMvc
            .post()
            .uri("/api/v1/auth/password-reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"$email"}""")
            .exchange()
    }

    private val perUserTables =
        listOf(
            "asset_entries",
            "income_sources",
            "fixed_expenses",
            "goals",
            "renewals",
            "schedule_events",
            "guide_journeys",
            "module_enrollments",
            "notification_preferences",
            "auth_credentials",
            "refresh_tokens",
            "password_reset_tokens",
        )

    @Test
    fun `탈퇴하면 모든 표에서 사라진다`() {
        val email = "withdraw-all@example.com"
        val token = signUp(email)
        seedEverything(token, email)
        val userId = userIdOf(email)

        // 심은 것이 실제로 들어갔는지 먼저 확인한다 — 안 그러면 "0 == 0" 으로 헛통과한다.
        perUserTables.forEach { table ->
            assertThat(countFor(table, userId)).describedAs("심기 전 $table").isPositive()
        }

        val response =
            mockMvc
                .delete()
                .uri("/api/v1/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
        assertThat(response.response.status).isEqualTo(HttpStatus.NO_CONTENT.value())

        perUserTables.forEach { table ->
            assertThat(countFor(table, userId)).describedAs("탈퇴 후 $table").isZero()
        }
    }

    @Test
    fun `가이드의 할 일 진행도 남지 않는다`() {
        val email = "withdraw-guide@example.com"
        val token = signUp(email)
        seedEverything(token, email)

        mockMvc
            .delete()
            .uri("/api/v1/me")
            .header(HttpHeaders.AUTHORIZATION, bearer(token))
            .exchange()

        // 진행은 journey_id 로만 묶여 있어 user_id 로 찾을 수 없다 — 고아 행이 남는지로 본다.
        val orphans =
            entityManager
                .createNativeQuery(
                    "select count(*) from guide_task_progresses p " +
                        "left join guide_journeys j on p.journey_id = j.id where j.id is null",
                ).singleResult as Long
        assertThat(orphans).isZero()
    }

    @Test
    fun `탈퇴한 이메일로 다시 가입할 수 있다`() {
        val email = "withdraw-reuse@example.com"
        val token = signUp(email)

        mockMvc
            .delete()
            .uri("/api/v1/me")
            .header(HttpHeaders.AUTHORIZATION, bearer(token))
            .exchange()

        // 상태만 바꿰 두면 유니크 인덱스가 이 주소를 영영 묶는다.
        signUp(email)
    }

    @Test
    fun `탈퇴하면 리프레시 쿠키를 지운다`() {
        val token = signUp("withdraw-cookie@example.com")

        val response =
            mockMvc
                .delete()
                .uri("/api/v1/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()

        val cookie = requireNotNull(response.response.getCookie("aetera_rt"))
        assertThat(cookie.maxAge).isZero()
    }
}
