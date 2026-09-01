package io.aetera.controller.asset

import io.aetera.model.asset.AssetCategory
import io.aetera.usecase.asset.cmd.SaveSnapshotCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class SnapshotReq(
    val entries: List<Entry>,
) {
    data class Entry(
        @field:Schema(example = "주거래 통장")
        val name: String,
        val category: AssetCategory,
        /** 원 단위. 부채도 양수로 적는다 — 부호는 분류가 정한다. */
        @field:Schema(example = "12000000")
        val amount: Long,
    )

    fun toCommand(
        userId: UUID,
        month: LocalDate,
    ): SaveSnapshotCommand = SaveSnapshotCommand(
        userId = userId,
        month = month,
        entries = entries.map { SaveSnapshotCommand.Entry(it.name, it.category, it.amount) },
    )
}
