package io.aetera.controller.common

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * 호출 한도 설정.
 *
 * 숫자를 코드에 박아 두지 않는 이유가 둘이다.
 *
 * 하나는 **공유 아이피**다. 회사·학교·통신사 NAT 뒤에서는 수백 명이 한 주소로 나오는데,
 * 그 전부를 한 사람으로 세면 정상 사용자가 서로를 막는다. 환경마다 알맞은 값이 다르므로
 * 배포하는 쪽이 정할 수 있어야 한다.
 *
 * 둘은 **시험**이다. 한도와 무관한 통합 시험들은 전부 같은 주소에서 수십 번 부르므로,
 * 끌 수 없으면 그쪽이 전부 429 로 무너진다 — 실제로 무너뜨려 보고 알았다.
 */
@ConfigurationProperties(prefix = "aetera.rate-limit")
data class RateLimitProperties(
    /** 끄면 인터셉터가 통과만 시킨다. 한도와 무관한 시험이 이걸 쓴다. */
    val enabled: Boolean = true,
    /** 비밀번호를 몇 번 헷갈리는 것은 흔하다. 몰아서 [Rule.capacity] 번, 이후 [Rule.refill] 마다 한 번. */
    val login: Rule = Rule(10, Duration.ofSeconds(30)),
    /**
     * 아이피가 아니라 **계정별**로 세는 로그인 한도.
     *
     * 아이피 한도는 한 대가 두드리는 것까지만 잡는다. 유출된 비밀번호 목록을 들고 한
     * 계정을 노리면 아이피를 흩어 때리면 그만이라, **노려지는 쪽에서 세야** 막힌다.
     * 비밀번호 찾기를 주소별로도 막은 것과 같은 이유다.
     */
    val loginPerAccount: Rule = Rule(10, Duration.ofMinutes(2)),
    /** 스팸 계정을 막되 NAT 뒤의 정상 가입은 막지 않을 만큼. */
    val signup: Rule = Rule(10, Duration.ofMinutes(2)),
    /** 메일을 보내는 길이라 가장 촘촘하다. 보내는 쪽 비용도 이쪽이 제일 크다. */
    val passwordReset: Rule = Rule(5, Duration.ofMinutes(3)),
    /** 토큰은 256비트라 찍어 맞힐 수 없다. 무차별 대입이 서버를 갉아먹는 것만 막는다. */
    val passwordResetConfirm: Rule = Rule(10, Duration.ofMinutes(1)),
    /**
     * 아이피가 아니라 **받는 주소**로 세는 한도.
     *
     * 아이피 한도는 한 대가 쏟아붓는 것까지만 잡는다. 한 사람의 메일함이 표적이면
     * 아이피를 바꿔 가며 때리면 그만이라, 받는 쪽에서 세야 막힌다.
     */
    val passwordResetPerAddress: Rule = Rule(3, Duration.ofMinutes(20)),
) {
    data class Rule(
        /** 한 번에 몰아 쓸 수 있는 횟수. */
        val capacity: Int,
        /** 한 번이 다시 차는 데 걸리는 시간. */
        val refill: Duration,
    ) {
        fun limiter(clock: java.time.Clock) = RateLimiter(capacity, refill, clock)
    }
}
