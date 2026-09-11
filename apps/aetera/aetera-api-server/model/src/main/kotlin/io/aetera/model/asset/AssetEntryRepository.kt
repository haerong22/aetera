package io.aetera.model.asset

import io.aetera.model.user.UserId
import java.time.LocalDate

interface AssetEntryRepository {
    fun saveAll(entries: List<AssetEntry>): List<AssetEntry>

    /** 최근 달부터. 화면 하나가 이 호출 한 번으로 그려진다. */
    fun findAllByUserId(userId: UserId): List<AssetEntry>

    /**
     * 그 기간에 속한 달만. 양 끝을 포함한다.
     *
     * 타임라인처럼 한 해치만 보는 쪽이 쓴다 — 전부 읽어 와서 메모리에서 자르면
     * 몇 해씩 기록한 사람의 표를 매번 통째로 읽는다. `(user_id, month)` 인덱스가 받친다.
     */
    fun findAllByUserIdAndMonthBetween(
        userId: UserId,
        from: LocalDate,
        to: LocalDate,
    ): List<AssetEntry>

    /** 한 달을 통째로 갈아 끼우기 전에 비운다. 스냅샷은 부분 수정이 아니라 교체다. */
    fun deleteByUserIdAndMonth(
        userId: UserId,
        month: LocalDate,
    )
}
