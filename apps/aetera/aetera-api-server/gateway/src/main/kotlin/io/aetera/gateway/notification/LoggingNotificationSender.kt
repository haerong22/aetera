package io.aetera.gateway.notification

import io.aetera.model.notification.NoticeDigest
import io.aetera.model.notification.NotificationSender
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

/**
 * 메일 대신 로그로 찍는 발송기.
 *
 * 실제 메일은 SMTP 자격증명이 있어야 보낼 수 있고, 그건 배포 환경의 몫이다. 그때까지
 * **알림이 만들어지는 것까지는 전부 돌아가야** 한다 — 무엇이 언제 누구에게 갈지를
 * 여기서 눈으로 확인할 수 있다.
 *
 * `aetera.notification.sender` 로 고른다. 기본이 `log` 라 아무 설정 없이도 서버가 뜬다 —
 * SMTP 발송기가 생기면 그쪽에 `havingValue = "email"` 을 달고 운영에서 속성만 바꾼다.
 *
 * 조건을 `@ConditionalOnMissingBean` 으로 두지 않는 이유: `@Component` 에서는 평가 순서가
 * 정해져 있지 않아 **둘 다 빠져 서버가 아예 안 뜨는 일**이 실제로 났다.
 *
 * 주소와 본문을 로그에 남기는 것은 **개인정보가 로그로 새는 일**이다. 운영에서 이 빈이
 * 뜨고 있다면 그건 설정이 빠진 것이지 의도가 아니다.
 */
@Component
@ConditionalOnProperty(name = ["aetera.notification.sender"], havingValue = "log", matchIfMissing = true)
class LoggingNotificationSender : NotificationSender {
    /** 메일 발송기와 **같은 글**을 찍는다. 다르면 여기서 본 것이 나갈 것과 달라져 볼 이유가 없다. */
    override fun send(digest: NoticeDigest) {
        log.info { "[알림·미발송] ${digest.to}\n제목: ${digest.subject}\n${digest.body}" }
    }
}
