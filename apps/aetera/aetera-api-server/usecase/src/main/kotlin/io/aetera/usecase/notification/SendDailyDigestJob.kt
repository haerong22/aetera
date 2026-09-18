package io.aetera.usecase.notification

import io.aetera.model.notification.NotificationPreferenceRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

/**
 * 하루치 알림을 내보낸다.
 *
 * **한 시간마다 돈다.** 하루 한 번 정해진 시각에 돌면 모든 사용자가 같은 순간에 받게 되어
 * 시간대가 다른 사람은 한밤중에 받는다. 매시 돌면서 "지금 그 사람에게 몇 시인가"를 보면
 * 각자의 아침에 닿는다.
 *
 * 매시 도는 대가는 같은 사람에게 여러 번 보낼 위험인데, 그건 `lastSentOn` 이 막는다.
 *
 * 한 사람의 실패로 그날 전체가 멈추면 안 되므로 사람마다 따로 감싼다.
 * 실제 발송과 트랜잭션은 [SendDigestService] 가 맡는다.
 */
@Component
class SendDailyDigestJob(
    private val notificationPreferenceRepository: NotificationPreferenceRepository,
    private val sendDigestService: SendDigestService,
) {
    /** 기본은 매시 정각. 속성으로 빼 둔 덕에 확인할 때는 짧게 돌려 볼 수 있다. */
    @Scheduled(cron = "\${aetera.notification.cron:0 0 * * * *}")
    fun run() {
        val sent =
            notificationPreferenceRepository.findAllEnabled().count { preference ->
                runCatching { sendDigestService.send(preference) }
                    .onFailure { log.warn(it) { "알림 발송 실패 userId=${preference.userId}" } }
                    .getOrDefault(false)
            }

        if (sent > 0) log.info { "알림 ${sent}통 발송" }
    }
}
