package io.aetera.gateway.income

import io.aetera.gateway.common.saveMerging
import io.aetera.model.income.IncomeSource
import io.aetera.model.income.IncomeSourceId
import io.aetera.model.income.IncomeSourceRepository
import io.aetera.model.user.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class IncomeSourceRepositoryJpaAdapter(
    private val incomeSourceJpaRepository: IncomeSourceJpaRepository,
) : IncomeSourceRepository {
    override fun save(source: IncomeSource): IncomeSource = incomeSourceJpaRepository
        .saveMerging(
            id = source.id.value,
            update = { it.applyFrom(source) },
            create = { IncomeSourceJpaEntity.from(source) },
        ).toModel()

    override fun getById(id: IncomeSourceId): IncomeSource? = incomeSourceJpaRepository.findByIdOrNull(id.value)?.toModel()

    override fun findAllByUserId(userId: UserId): List<IncomeSource> = incomeSourceJpaRepository
        .findAllByUserId(userId.value)
        .map { it.toModel() }

    override fun delete(source: IncomeSource) {
        incomeSourceJpaRepository.deleteById(source.id.value)
    }
}
