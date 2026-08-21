package io.aetera.gateway.renewal

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.renewal.Renewal
import io.aetera.model.renewal.RenewalCategory
import io.aetera.model.renewal.RenewalCycle
import io.aetera.model.renewal.RenewalId
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
@Table(name = "renewals")
class RenewalJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "title", nullable = false, length = 100)
    var title: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    var category: RenewalCategory,
    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDate,
    @Enumerated(EnumType.STRING)
    @Column(name = "cycle", nullable = false, length = 20)
    var cycle: RenewalCycle,
    @Column(name = "notice_days", nullable = false)
    var noticeDays: Int,
    @Column(name = "memo", columnDefinition = "text")
    var memo: String?,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
) : UuidJpaEntity(uid) {
    fun applyFrom(renewal: Renewal) {
        title = renewal.title
        category = renewal.category
        expiresAt = renewal.expiresAt
        cycle = renewal.cycle
        noticeDays = renewal.noticeDays
        memo = renewal.memo
    }

    fun toModel(): Renewal = Renewal.reconstitute(
        id = RenewalId(uid),
        userId = UserId(userId),
        title = title,
        category = category,
        expiresAt = expiresAt,
        cycle = cycle,
        noticeDays = noticeDays,
        memo = memo,
        createdAt = createdAt,
    )

    companion object {
        fun from(renewal: Renewal): RenewalJpaEntity = RenewalJpaEntity(
            uid = renewal.id.value,
            userId = renewal.userId.value,
            title = renewal.title,
            category = renewal.category,
            expiresAt = renewal.expiresAt,
            cycle = renewal.cycle,
            noticeDays = renewal.noticeDays,
            memo = renewal.memo,
            createdAt = renewal.createdAt,
        )
    }
}
