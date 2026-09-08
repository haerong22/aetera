package io.aetera.usecase.asset

import io.aetera.model.asset.AssetEntryRepository
import io.aetera.model.asset.netWorth
import io.aetera.model.module.ModuleId
import io.aetera.model.module.TimelineContributor
import io.aetera.model.module.TimelineEntry
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.Locale

/**
 * 달마다 찍은 순자산을 타임라인에 낸다.
 *
 * 자산은 지나간 것을 실제로 들고 있는 몇 안 되는 모듈이라 타임라인이 보여 줄 게 가장 많다.
 * 항목 하나하나는 내지 않는다 — "통장 1,200만원"은 그 달의 부분이지 이정표가 아니다.
 */
@Component
class AssetTimelineContributor(
    private val assetEntryRepository: AssetEntryRepository,
) : TimelineContributor {
    override val moduleIds: Set<ModuleId> = setOf(AssetModule.MODULE_ID)

    override fun entriesIn(
        userId: UserId,
        from: LocalDate,
        to: LocalDate,
    ): List<TimelineEntry> = assetEntryRepository
        .findAllByUserId(userId)
        .groupBy { it.month }
        .filterKeys { it in from..to }
        .map { (month, entries) ->
            TimelineEntry(
                moduleId = AssetModule.MODULE_ID,
                on = month,
                title = "순자산 ${money(entries.netWorth())}원",
            )
        }

    /**
     * 로케일을 못 박는다. 기본 로케일에 맡기면 컨테이너 설정에 따라 자릿점이 달라져
     * 같은 코드가 `48.200.000` 이나 `4,82,00,000` 을 낸다.
     */
    private fun money(amount: Long): String = String.format(Locale.KOREA, "%,d", amount)
}
