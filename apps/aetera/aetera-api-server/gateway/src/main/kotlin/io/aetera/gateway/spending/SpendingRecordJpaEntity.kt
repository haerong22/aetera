package io.aetera.gateway.spending

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.spending.SpendingRecord
import io.aetera.model.spending.SpendingRecordId
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "spending_records")
class SpendingRecordJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "month", nullable = false, updatable = false)
    val month: LocalDate,
    @Column(name = "amount", nullable = false)
    var amount: Long,
    @Column(name = "note", length = 200)
    var note: String?,
    @Column(name = "recorded_at", nullable = false)
    var recordedAt: Instant,
) : UuidJpaEntity(uid) {
    fun applyFrom(record: SpendingRecord) {
        amount = record.amount
        note = record.note
        recordedAt = record.recordedAt
    }

    fun toModel(): SpendingRecord = SpendingRecord.reconstitute(
        id = SpendingRecordId(uid),
        userId = UserId(userId),
        month = month,
        amount = amount,
        note = note,
        recordedAt = recordedAt,
    )

    companion object {
        fun from(record: SpendingRecord): SpendingRecordJpaEntity = SpendingRecordJpaEntity(
            uid = record.id.value,
            userId = record.userId.value,
            month = record.month,
            amount = record.amount,
            note = record.note,
            recordedAt = record.recordedAt,
        )
    }
}
