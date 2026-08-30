package io.aetera.model.expense

import io.aetera.model.user.UserId

interface FixedExpenseRepository {
    fun save(expense: FixedExpense): FixedExpense

    fun getById(id: FixedExpenseId): FixedExpense?

    /** 등록 순. 화면에 보이는 순서(부담 큰 순)는 파생값이라 유스케이스가 정한다. */
    fun findAllByUserId(userId: UserId): List<FixedExpense>

    fun delete(expense: FixedExpense)
}
