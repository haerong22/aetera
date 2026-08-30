package io.aetera.gateway.expense

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.expense.ExpenseCategory
import io.aetera.model.expense.ExpenseCycle
import io.aetera.model.expense.FixedExpense
import io.aetera.model.expense.FixedExpenseId
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "fixed_expenses")
class FixedExpenseJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "title", nullable = false, length = 100)
    var title: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    var category: ExpenseCategory,
    @Column(name = "amount", nullable = false)
    var amount: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "cycle", nullable = false, length = 20)
    var cycle: ExpenseCycle,
    @Column(name = "memo", columnDefinition = "text")
    var memo: String?,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
) : UuidJpaEntity(uid) {
    fun applyFrom(expense: FixedExpense) {
        title = expense.title
        category = expense.category
        amount = expense.amount
        cycle = expense.cycle
        memo = expense.memo
    }

    fun toModel(): FixedExpense = FixedExpense.reconstitute(
        id = FixedExpenseId(uid),
        userId = UserId(userId),
        title = title,
        category = category,
        amount = amount,
        cycle = cycle,
        memo = memo,
        createdAt = createdAt,
    )

    companion object {
        fun from(expense: FixedExpense): FixedExpenseJpaEntity = FixedExpenseJpaEntity(
            uid = expense.id.value,
            userId = expense.userId.value,
            title = expense.title,
            category = expense.category,
            amount = expense.amount,
            cycle = expense.cycle,
            memo = expense.memo,
            createdAt = expense.createdAt,
        )
    }
}
