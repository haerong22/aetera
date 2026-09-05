package io.aetera.model.expense

import io.aetera.model.user.UserId

interface FixedExpenseRepository {
    fun save(expense: FixedExpense): FixedExpense

    fun getById(id: FixedExpenseId): FixedExpense?

    /** 순서는 정하지 않는다 — 화면에 보이는 순서(부담 큰 순)가 파생값이라 유스케이스가 어차피 다시 정렬한다. */
    fun findAllByUserId(userId: UserId): List<FixedExpense>

    fun delete(expense: FixedExpense)
}
