package io.aetera.usecase.renewal

import io.aetera.model.module.ModuleId
import io.aetera.model.module.TimelineContributor
import io.aetera.model.module.TimelineEntry
import io.aetera.model.renewal.RenewalRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 만기일을 타임라인에 낸다.
 *
 * 갱신하면 만기일이 다음 주기로 굴러가므로 **지난 만기는 기록에 남지 않는다.**
 * 지금 잡혀 있는 만기 하나만 나온다 — 갱신 이력을 남기려면 만기 관리 쪽이 따로 쌓아야 하고,
 * 그건 필요해질 때 할 일이다.
 */
@Component
class RenewalTimelineContributor(
    private val renewalRepository: RenewalRepository,
) : TimelineContributor {
    override val moduleIds: Set<ModuleId> = setOf(RenewalModule.MODULE_ID)

    override fun entriesIn(
        userId: UserId,
        from: LocalDate,
        to: LocalDate,
    ): List<TimelineEntry> = renewalRepository
        .findAllByUserId(userId)
        .filter { it.expiresAt in from..to }
        .map { TimelineEntry(moduleId = RenewalModule.MODULE_ID, on = it.expiresAt, title = "${it.title} 만기") }
}
