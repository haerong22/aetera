package io.aetera.gateway.asset

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.asset.AssetCategory
import io.aetera.model.asset.AssetEntry
import io.aetera.model.asset.AssetEntryId
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "asset_entries")
class AssetEntryJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "month", nullable = false, updatable = false)
    val month: LocalDate,
    @Column(name = "name", nullable = false, length = 100, updatable = false)
    val name: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20, updatable = false)
    val category: AssetCategory,
    @Column(name = "amount", nullable = false, updatable = false)
    val amount: Long,
    @Column(name = "recorded_at", nullable = false, updatable = false)
    val recordedAt: Instant,
) : UuidJpaEntity(uid) {
    fun toModel(): AssetEntry = AssetEntry.reconstitute(
        id = AssetEntryId(uid),
        userId = UserId(userId),
        month = month,
        name = name,
        category = category,
        amount = amount,
        recordedAt = recordedAt,
    )

    companion object {
        fun from(entry: AssetEntry): AssetEntryJpaEntity = AssetEntryJpaEntity(
            uid = entry.id.value,
            userId = entry.userId.value,
            month = entry.month,
            name = entry.name,
            category = entry.category,
            amount = entry.amount,
            recordedAt = entry.recordedAt,
        )
    }
}
