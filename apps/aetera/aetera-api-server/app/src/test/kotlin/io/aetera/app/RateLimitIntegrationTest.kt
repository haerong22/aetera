package io.aetera.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.util.UUID

/**
 * 호출 한도를 **진짜 HTTP 로** 확인한다.
 *
 * 한도 계산 자체는 [RateLimiterTest][io.aetera.controller.common.RateLimiterTest] 가 본다.
 * 여기서 볼 것은 그 계산이 **실제 요청 경로에 걸려 있는가**다 — 인터셉터가 인증보다 앞에
 * 서 있는지, 경로가 맞는지, 429 와 `Retry-After` 가 나가는지는 배선 문제라 여기서만 드러난다.
 *
 * 한도는 서버 한 대의 기억이라 이 클래스 안의 시험들이 **서로 한도를 나눠 쓴다.**
 * 그래서 시험마다 다른 주소를 쓰고, 넉넉한 쪽이 아니라 **막히는 것**을 단언한다.
 */
@AeteraIntegrationTest
/*
 * **이 클래스만 한도를 다시 켠다.** [AeteraIntegrationTest] 는 꺼 둔다 — 다른 시험들은
 * 같은 주소에서 수십 번 부르므로 켜 두면 엉뚱한 이유로 깨진다. 여기서 볼 것이 한도다.
 *
 * `@TestPropertySource` 가 메타 애너테이션의 속성을 덮는다(우선순위가 더 높다).
 * 속성이 달라 스프링 컨텍스트도 따로 뜨므로, 다른 클래스가 쓴 한도가 섞이지 않는다.
 */
@TestPropertySource(properties = ["aetera.rate-limit.enabled=true"])
class RateLimitIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var outbox: Outbox

    /** 컨텍스트를 돌려 쓰므로 앞 클래스가 남긴 메일이 보인다 — 읽는 쪽이 비운다. */
    @BeforeEach
    fun clearOutbox() {
        outbox.reset()
    }

    private fun newEmail() = "ratelimit-${UUID.randomUUID()}@example.com"

    /**
     * 시험마다 새 아이피.
     *
     * 준비 단계(가입)가 **다른 시험이 써 버린 한도에 막히면** 계정이 안 만들어지고,
     * 그러면 뒤이은 단언이 엉뚱한 이유로 실패한다 — 실제로 한 번 그렇게 걸렸다.
     * 한도를 보려는 시험만 아이피를 고정한다.
     */
    private val ipSequence =
        java.util.concurrent.atomic
            .AtomicInteger()

    private fun freshIp(): String {
        val n = ipSequence.incrementAndGet()
        return "172.16.${n / 250}.${n % 250}"
    }

    private fun login(
        email: String,
        from: String = "127.0.0.1",
    ) = mockMvc
        .post()
        .uri("/api/v1/auth/login")
        .remoteAddress(from)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"email":"$email","password":"wrongpassword1"}""")
        .exchange()

    /**
     * [from] 으로 아이피를 바꿔 가며 부를 수 있다.
     *
     * MockMvc 는 두지 않으면 모든 요청이 같은 주소라 **아이피 한도에 먼저 걸린다.**
     * 그러면 주소별 한도까지 닿지 못해, 그걸 보려던 시험이 엉뚱한 것을 재게 된다.
     * 아이피를 흩는 것은 흉내가 아니라 **실제 메일 폭탄의 모습**이기도 하다.
     */
    private fun requestReset(
        email: String,
        from: String = "127.0.0.1",
    ) = mockMvc
        .post()
        .uri("/api/v1/auth/password-reset")
        .remoteAddress(from)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"email":"$email"}""")
        .exchange()

    private fun signUp(
        email: String,
        from: String = freshIp(),
    ) = mockMvc
        .post()
        .uri("/api/v1/auth/signup")
        .remoteAddress(from)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"email":"$email","nickname":"홍길동","password":"password1234"}""")
        .exchange()

    /** 429 가 나올 때까지 부른다. 나온 횟수를 돌려준다 — 안 나오면 0. */
    private fun callUntilLimited(
        times: Int,
        call: () -> org.springframework.test.web.servlet.assertj.MvcTestResult,
    ): Int = (1..times).count { call().response.status == HttpStatus.TOO_MANY_REQUESTS.value() }

    /**
     * 로그인이 이 기능의 가장 큰 이유다. 막지 않으면 비밀번호를 끝없이 찍어 볼 수 있다.
     * 인터셉터가 인증 **뒤에** 서 있으면 이 시험이 실패한다 — 로그인은 인증을 통과하지 않는다.
     */
    @Test
    fun `로그인을 계속 두드리면 막는다`() {
        val email = newEmail()

        val blocked = callUntilLimited(40) { login(email) }

        assertThat(blocked).isPositive()
    }

    /** 언제 다시 되는지 알려 주지 않으면 클라이언트가 계속 두드려, 막으려던 부하가 그대로 생긴다. */
    @Test
    fun `막을 때 다시 시도할 시각을 알려 준다`() {
        val email = newEmail()
        repeat(40) { login(email) }

        val response = login(email)

        assertThat(response.response.status).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value())
        val retryAfter = requireNotNull(response.response.getHeader(HttpHeaders.RETRY_AFTER))
        assertThat(retryAfter.toLong()).isPositive()

        // 헤더는 기계만 읽는다. 사람이 보는 본문에도 같은 값이 있어야 "잠시"가 얼마인지 안다.
        assertThat(response.response.contentAsString).contains("${retryAfter}초")
    }

    /**
     * 아이피 한도만으로는 유출된 비밀번호 목록을 한 계정에 들이붓는 것을 못 막는다 —
     * 아이피를 흩으면 그만이다. 노려지는 계정 쪽에서 세야 걸린다.
     *
     * 비밀번호 찾기를 주소별로도 막은 것과 **같은 근거**다. 한쪽에만 두면 일관성이 깨진다.
     */
    @Test
    fun `아이피를 바꿔 가며 한 계정을 두드리면 막는다`() {
        val email = newEmail()

        // 아이피가 매번 달라 아이피 한도는 한 번도 안 걸린다 — 계정별 한도만 남는다.
        val statuses = (1..15).map { login(email, from = "10.2.0.$it").response.status }

        assertThat(statuses).contains(HttpStatus.TOO_MANY_REQUESTS.value())
    }

    /** 한 계정이 한도를 써도 다른 계정은 계속 시도할 수 있어야 한다. */
    @Test
    fun `한 계정이 한도를 써도 다른 계정은 막지 않는다`() {
        val hammered = newEmail()
        (1..15).forEach { login(hammered, from = "10.3.0.$it") }

        val other = login(newEmail(), from = "10.3.0.99")

        // 없는 계정이라 401 — 429 가 아니어야 한다.
        assertThat(other.response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
    }

    /**
     * 계정별 한도가 가입 여부를 흘리면 안 된다. **적어 낸 주소로** 세므로 없는 계정을
     * 두드려도 똑같이 429 가 돌아온다.
     */
    @Test
    fun `없는 계정을 두드려도 똑같이 막는다`() {
        val missing = "nobody-${UUID.randomUUID()}@example.com"

        val statuses = (1..15).map { login(missing, from = "10.4.0.$it").response.status }

        assertThat(statuses).contains(HttpStatus.TOO_MANY_REQUESTS.value())
    }

    @Test
    fun `가입도 막는다`() {
        // 한 아이피에서 쏟아붓는 경우다 — 여기만 고정한다.
        val blocked = callUntilLimited(20) { signUp(newEmail(), from = "203.0.113.7") }

        assertThat(blocked).isPositive()
    }

    /**
     * 주소별 한도. 아이피 한도는 한 대가 쏟아붓는 것까지만 잡으므로, 한 사람의 메일함이
     * 표적일 때는 받는 쪽에서 세야 막힌다.
     *
     * **응답은 끝까지 204 여야 한다.** 429 로 답하면 "한도가 찼다 = 아까 진짜로 보냈다"가
     * 되어 가입 여부가 새어 나간다 — 비밀번호 찾기가 통째로 막으려던 그것이다.
     */
    @Test
    fun `아이피를 바꿔 가며 때려도 메일은 한도까지만 나간다`() {
        val email = newEmail()
        signUp(email)
        outbox.sent.clear()

        // 아이피가 매번 달라 아이피 한도는 한 번도 안 걸린다 — 주소별 한도만 남는다.
        val statuses = (1..10).map { requestReset(email, from = "10.0.0.$it").response.status }

        assertThat(statuses).allMatch { it == HttpStatus.NO_CONTENT.value() }
        assertThat(outbox.sent).hasSize(3)
    }

    /** 한도에 걸린 요청이 남의 한도까지 쓰면 안 된다 — 한 사람이 모두를 막는 셈이 된다. */
    @Test
    fun `한 주소가 한도를 써도 다른 주소는 메일을 받는다`() {
        val victim = newEmail()
        val other = newEmail()
        signUp(other)
        (1..10).forEach { requestReset(victim, from = "10.1.0.$it") }
        outbox.sent.clear()

        val response = requestReset(other, from = "10.1.0.99")

        assertThat(response.response.status).isEqualTo(HttpStatus.NO_CONTENT.value())
        assertThat(outbox.sent.map { it.to.value }).contains(other)
    }

    /** 한도는 인증이 필요 없는 길에만 건다. 로그인한 뒤의 API 까지 막으면 정상 사용이 끊긴다. */
    @Test
    fun `로그인한 뒤의 API 는 막지 않는다`() {
        val email = newEmail()
        val token =
            com.jayway.jsonpath.JsonPath.read<String>(
                signUp(email).response.contentAsString,
                "$.accessToken",
            )

        val statuses =
            (1..40).map {
                mockMvc
                    .get()
                    .uri("/api/v1/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .exchange()
                    .response.status
            }

        assertThat(statuses).allMatch { it == HttpStatus.OK.value() }
    }
}
