package io.aetera.model.asset

import io.aetera.model.user.UserId
import java.time.LocalDate

interface AssetEntryRepository {
    fun saveAll(entries: List<AssetEntry>): List<AssetEntry>

    /** 최근 달부터. 화면 하나가 이 호출 한 번으로 그려진다. */
    fun findAllByUserId(userId: UserId): List<AssetEntry>

    /** 한 달을 통째로 갈아 끼우기 전에 비운다. 스냅샷은 부분 수정이 아니라 교체다. */
    fun deleteByUserIdAndMonth(
        userId: UserId,
        month: LocalDate,
    )
}
