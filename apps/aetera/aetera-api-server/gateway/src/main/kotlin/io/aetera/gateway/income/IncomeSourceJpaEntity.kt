package io.aetera.gateway.income

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.income.IncomeCategory
import io.aetera.model.income.IncomeCycle
import io.aetera.model.income.IncomeSource
import io.aetera.model.income.IncomeSourceId
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "income_sources")
class IncomeSourceJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "title", nullable = false, length = 100)
    var title: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    var category: IncomeCategory,
    @Column(name = "amount", nullable = false)
    var amount: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "cycle", nullable = false, length = 20)
    var cycle: IncomeCycle,
    @Column(name = "memo", columnDefinition = "text")
    var memo: String?,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
) : UuidJpaEntity(uid) {
    fun applyFrom(source: IncomeSource) {
        title = source.title
        category = source.category
        amount = source.amount
        cycle = source.cycle
        memo = source.memo
    }

    fun toModel(): IncomeSource = IncomeSource.reconstitute(
        id = IncomeSourceId(uid),
        userId = UserId(userId),
        title = title,
        category = category,
        amount = amount,
        cycle = cycle,
        memo = memo,
        createdAt = createdAt,
    )

    companion object {
        fun from(source: IncomeSource): IncomeSourceJpaEntity = IncomeSourceJpaEntity(
            uid = source.id.value,
            userId = source.userId.value,
            title = source.title,
            category = source.category,
            amount = source.amount,
            cycle = source.cycle,
            memo = source.memo,
            createdAt = source.createdAt,
        )
    }
}
