package io.aetera.gateway.expense

import io.aetera.gateway.common.saveMerging
import io.aetera.model.expense.FixedExpense
import io.aetera.model.expense.FixedExpenseId
import io.aetera.model.expense.FixedExpenseRepository
import io.aetera.model.user.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class FixedExpenseRepositoryJpaAdapter(
    private val fixedExpenseJpaRepository: FixedExpenseJpaRepository,
) : FixedExpenseRepository {
    override fun save(expense: FixedExpense): FixedExpense = fixedExpenseJpaRepository
        .saveMerging(
            id = expense.id.value,
            update = { it.applyFrom(expense) },
            create = { FixedExpenseJpaEntity.from(expense) },
        ).toModel()

    override fun getById(id: FixedExpenseId): FixedExpense? = fixedExpenseJpaRepository.findByIdOrNull(id.value)?.toModel()

    override fun findAllByUserId(userId: UserId): List<FixedExpense> = fixedExpenseJpaRepository
        .findAllByUserId(userId.value)
        .map { it.toModel() }

    override fun delete(expense: FixedExpense) {
        fixedExpenseJpaRepository.deleteById(expense.id.value)
    }
}
