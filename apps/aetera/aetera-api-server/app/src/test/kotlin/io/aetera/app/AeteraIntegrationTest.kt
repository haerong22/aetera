package io.aetera.app

import org.junit.jupiter.api.Tag
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import

/**
 * 통합 시험 한 벌의 배선.
 *
 * 같은 애너테이션 블록이 일곱 벌로 복제돼 있었다. 단순 중복이 아니라 **함정**이었다 —
 * 새 시험에서 `aetera.rate-limit.enabled=false` 를 빠뜨리면 같은 주소에서 수십 번 부르다
 * 429 에 걸려 **엉뚱한 이유로 깨진다.** 한 번 그렇게 헤맸다.
 *
 * ## 담긴 것
 *
 * - `security.password.iterations=1000` — 기본 반복 횟수로는 시험이 몇 분씩 걸린다
 * - `aetera.rate-limit.enabled=false` — 아래 설명
 * - Testcontainers 로 띄운 진짜 Postgres
 * - [TestMailbox] — 메일을 붙잡아 두는 발송기
 *
 * ## 호출 한도를 끄는 이유
 *
 * 통합 시험은 전부 같은 주소(`127.0.0.1`)에서 수십 번 부른다. 켜 두면 한도에 걸려 무너지는데,
 * 여기서 볼 것은 한도가 아니다. 한도 자체는 [RateLimitIntegrationTest] 가 보며, 그 클래스만
 * `@TestPropertySource` 로 다시 켠다.
 *
 * ## 스프링 컨텍스트를 나눠 쓴다
 *
 * 애너테이션이 같은 클래스들은 스프링이 컨텍스트를 **하나 만들어 돌려 쓴다.** 빠르지만
 * [Outbox] 같은 상태 있는 빈도 함께 공유되므로, 메일을 읽는 시험은 시작할 때 비워야 한다 —
 * 그 규약은 [Outbox] 에 적어 두었다.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Tag("integration")
@SpringBootTest(
    properties = [
        "security.password.iterations=1000",
        "aetera.rate-limit.enabled=false",
    ],
)
@AutoConfigureMockMvc
@Import(TestcontainersConfig::class, TestMailbox::class)
annotation class AeteraIntegrationTest
