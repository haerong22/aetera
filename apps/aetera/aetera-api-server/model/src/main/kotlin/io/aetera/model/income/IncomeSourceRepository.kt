package io.aetera.model.income

import io.aetera.model.user.UserId

interface IncomeSourceRepository {
    fun save(source: IncomeSource): IncomeSource

    fun getById(id: IncomeSourceId): IncomeSource?

    /** 순서는 정하지 않는다 — 화면에 보이는 순서(큰 것부터)가 파생값이라 유스케이스가 어차피 다시 정렬한다. */
    fun findAllByUserId(userId: UserId): List<IncomeSource>

    fun delete(source: IncomeSource)
}
