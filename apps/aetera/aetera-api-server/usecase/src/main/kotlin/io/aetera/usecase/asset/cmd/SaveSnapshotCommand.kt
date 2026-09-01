package io.aetera.usecase.asset.cmd

import io.aetera.model.asset.AssetCategory
import java.time.LocalDate
import java.util.UUID

/**
 * 한 달치를 통째로 받는다. 줄 하나만 고치는 명령을 두지 않는 이유는
 * 스냅샷이 "그 달의 상태 전체"라서다 — 부분 수정은 반쪽짜리 사실을 만든다.
 */
data class SaveSnapshotCommand(
    val userId: UUID,
    val month: LocalDate,
    val entries: List<Entry>,
) {
    data class Entry(
        val name: String,
        val category: AssetCategory,
        val amount: Long,
    )
}
