package io.aetera.usecase.notification

import io.aetera.model.module.ModuleEnrollmentRepository
import io.aetera.model.module.NotificationContributor
import io.aetera.model.notification.NoticeDigest
import io.aetera.model.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 한 사람에게 오늘 보낼 것을 모은다. 보낼 게 없으면 `null`.
 *
 * **어떤 모듈이 있는지 모른다.** 스프링이 [NotificationContributor] 빈을 전부 넣어 주고,
 * 그중 사용자가 켠 것만 부른다 — 타임라인과 같은 방식이다.
 *
 * 꺼진 모듈은 부르지 않는다. 끈 모듈이 메일을 보내오면 "중지"가 무슨 뜻인지 알 수 없게 된다.
 */
@Service
@Transactional(readOnly = true)
class BuildDigestService(
    private val contributors: List<NotificationContributor>,
    private val moduleEnrollmentRepository: ModuleEnrollmentRepository,
) {
    fun build(
        user: User,
        localDate: LocalDate,
    ): NoticeDigest? {
        val enabled =
            moduleEnrollmentRepository
                .findAllByUserId(user.id)
                .filter { it.isEnabled }
                .map { it.moduleId }
                .toSet()

        val notices =
            contributors
                .filter { contributor -> contributor.moduleIds.any { it in enabled } }
                .flatMap { it.noticesFor(user.id, localDate) }
                // 기여자 하나가 여러 모듈을 낼 수 있으므로 줄 단위로 한 번 더 거른다.
                .filter { it.moduleId in enabled }
                // 가까운 것부터. 이미 지난 만기가 맨 위다.
                .sortedWith(compareBy<io.aetera.model.module.Notice> { it.on }.thenBy { it.title })

        // 빈 다이제스트는 만들지 않는다. "오늘은 알릴 게 없습니다"를 매일 보내면 스팸이 된다.
        if (notices.isEmpty()) return null

        return NoticeDigest(to = user.email, nickname = user.nickname, on = localDate, notices = notices)
    }
}
