package io.aetera.usecase.asset

import io.aetera.model.asset.AssetCategory
import io.aetera.model.asset.AssetEntry
import io.aetera.model.asset.cashTotal
import io.aetera.model.asset.netWorth
import java.time.LocalDate

data class AssetEntryDto(
    val name: String,
    val category: AssetCategory,
    val amount: Long,
    /**
     * 순자산에 더해질 부호 있는 금액. 부채면 음수다.
     *
     * 분류마다 빼는 쪽인지를 화면이 다시 판단하지 않게 여기서 정해 준다 — 그 표를 프론트에도
     * 두면 분류가 하나 늘 때 두 언어를 맞춰야 하고, 한쪽만 빠뜨리면 순자산이 조용히 어긋난다.
     */
    val signedAmount: Long,
) {
    constructor(entry: AssetEntry) : this(entry.name, entry.category, entry.amount, entry.signedAmount)
}

/** 순자산 추이의 한 점. */
data class AssetPointDto(
    val month: LocalDate,
    val netWorth: Long,
)

/**
 * 자산 화면 하나를 그리는 데 필요한 전부.
 *
 * `latestMonth` 가 null 이면 아직 한 번도 기록하지 않은 상태다 — 오류가 아니라 시작 전이다.
 * "이번 달을 이미 적었는가"는 브라우저가 자기 달력으로 판단한다. 서버는 사용자의 오늘을 모른다.
 */
data class AssetBoardDto(
    val latestMonth: LocalDate?,
    val entries: List<AssetEntryDto>,
    val netWorth: Long,
    /** 당장 쓸 수 있는 돈. 다른 모듈이 "몇 달 버티나"를 물을 때 쓴다. */
    val cashTotal: Long,
    /** 직전 기록 대비 증감. 기록이 하나뿐이면 null. */
    val changeFromPrevious: Long?,
    /** 순자산 추이. 오래된 것부터. */
    val history: List<AssetPointDto>,
) {
    companion object {
        /** 화면이 그리는 만큼만 내려보낸다. 몇 년을 적어도 응답이 무한정 길어지지 않는다. */
        private const val HISTORY_MONTHS = 24

        fun of(entries: List<AssetEntry>): AssetBoardDto {
            val byMonth = entries.groupBy { it.month }
            val months = byMonth.keys.sorted()
            val latest = months.lastOrNull()
            val current = latest?.let { byMonth.getValue(it) }.orEmpty()

            val history = months.takeLast(HISTORY_MONTHS).map { AssetPointDto(it, byMonth.getValue(it).netWorth()) }
            val previous = months.getOrNull(months.size - 2)?.let { byMonth.getValue(it).netWorth() }

            return AssetBoardDto(
                latestMonth = latest,
                entries = current.map(::AssetEntryDto),
                netWorth = current.netWorth(),
                cashTotal = current.cashTotal(),
                changeFromPrevious = previous?.let { current.netWorth() - it },
                history = history,
            )
        }
    }
}
