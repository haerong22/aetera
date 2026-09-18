package io.aetera.usecase.renewal

import io.aetera.model.module.ModuleId
import io.aetera.model.module.Notice
import io.aetera.model.module.NotificationContributor
import io.aetera.model.renewal.RenewalRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 만기가 다가온 것을 알린다.
 *
 * **항목마다 언제부터 급한지가 다르다** — 여권은 6개월 전, 보험은 한 달 전. 그 판단은
 * 항목이 들고 있는 `noticeDays` 가 하고, 규칙은 모델([Renewal.needsNotice])에 있다.
 *
 * 지난 만기도 낸다. 갱신하지 않은 채 만기가 지났다면 그게 가장 급한 일이고,
 * 조용해지는 순간 놓친 것을 영영 모른다.
 */
@Component
class RenewalNotificationContributor(
    private val renewalRepository: RenewalRepository,
) : NotificationContributor {
    override val moduleIds: Set<ModuleId> = setOf(RenewalModule.MODULE_ID)

    override fun noticesFor(
        userId: UserId,
        today: LocalDate,
    ): List<Notice> = renewalRepository
        .findAllByUserId(userId)
        .filter { it.needsNotice(today) }
        .map { renewal ->
            Notice(
                moduleId = RenewalModule.MODULE_ID,
                on = renewal.expiresAt,
                title = "${renewal.title} 만기",
                detail = detailFor(renewal.expiresAt, today),
            )
        }

    private fun detailFor(
        expiresAt: LocalDate,
        today: LocalDate,
    ): String {
        val days = expiresAt.toEpochDay() - today.toEpochDay()
        return when {
            days < 0 -> "${-days}일 지났어요"
            days == 0L -> "오늘이에요"
            else -> "${days}일 남았어요"
        }
    }
}
