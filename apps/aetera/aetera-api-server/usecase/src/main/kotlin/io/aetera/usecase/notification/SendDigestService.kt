package io.aetera.usecase.notification

import io.aetera.model.notification.NoticeDigest
import io.aetera.model.notification.NotificationPreference
import io.aetera.model.notification.NotificationPreferenceRepository
import io.aetera.model.notification.NotificationSender
import io.aetera.model.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime

/** 보낼 것과 그 사람 기준의 날짜. 날짜를 함께 들고 다니는 이유는 보낸 뒤에 그대로 표시하기 위해서다. */
private data class Plan(
    val digest: NoticeDigest,
    val on: LocalDate,
)

/**
 * 한 사람에게 오늘치를 보낸다. 보낼 때가 아니거나 보낼 게 없으면 `false`.
 *
 * **발송은 트랜잭션 밖에서 한다.** SMTP 왕복은 몇 초가 걸릴 수 있는데 그동안 DB 커넥션을
 * 붙들고 있으면, 사용자가 늘수록 잡이 도는 시간만큼 풀이 마르고 일반 요청이 막힌다.
 * 그래서 읽기와 쓰기를 따로 열고 그 사이에 내보낸다.
 *
 * 경계를 애너테이션이 아니라 [TransactionTemplate] 으로 긋는다. 한 클래스 안에서
 * `@Transactional` 메서드를 부르면 스프링 프록시를 거치지 않아 **조용히 아무 일도 하지 않고**,
 * 그걸 피하려고 클래스를 셋으로 쪼개면 정작 순서가 어디에도 안 보이게 된다.
 */
@Service
class SendDigestService(
    private val transactionTemplate: TransactionTemplate,
    private val notificationPreferenceRepository: NotificationPreferenceRepository,
    private val userRepository: UserRepository,
    private val buildDigestService: BuildDigestService,
    private val sender: NotificationSender,
    private val clock: Clock,
) {
    fun send(preference: NotificationPreference): Boolean {
        val plan = transactionTemplate.execute { planFor(preference) } ?: return false

        /*
         * 보낸 뒤에 표시한다. 순서를 뒤집어 "보냈다"고 먼저 적으면 발송이 실패한 날의 알림이
         * 조용히 사라지고, 사용자는 만기를 놓치고도 이유를 모른다.
         *
         * 이 순서의 대가는 **보낸 뒤 저장이 실패하면 다음 시각에 한 번 더 간다**는 것이다.
         * 알고 고른 쪽이다 — 같은 메일을 두 번 받는 것보다 놓치는 쪽이 훨씬 나쁘다.
         */
        sender.send(plan.digest)
        transactionTemplate.execute {
            preference.markSent(plan.on)
            notificationPreferenceRepository.save(preference)
        }
        return true
    }

    private fun planFor(preference: NotificationPreference): Plan? {
        val user = userRepository.getById(preference.userId)?.takeIf { it.isActive } ?: return null

        val now = ZonedDateTime.now(clock.withZone(user.timezone))
        if (!preference.shouldSend(now.toLocalDate(), now.hour)) return null

        val digest = buildDigestService.build(user, now.toLocalDate()) ?: return null
        return Plan(digest, now.toLocalDate())
    }
}
