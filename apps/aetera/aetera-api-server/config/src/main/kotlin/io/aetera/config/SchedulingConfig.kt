package io.aetera.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * `@Scheduled` 를 켠다. 지금 도는 것은 알림 발송 하나뿐이다.
 *
 * 서버를 여러 대로 늘리면 잡이 대수만큼 돈다. 같은 사람에게 여러 통이 가지는 않는데,
 * `notification_preferences.last_sent_on` 이 같은 행을 두고 다투게 되어 한쪽만 이기기
 * 때문이다. 그래도 헛도는 것은 사실이라, 대수를 늘릴 때는 잡을 한 대에만 두거나
 * 분산 락을 붙여야 한다.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
class SchedulingConfig
