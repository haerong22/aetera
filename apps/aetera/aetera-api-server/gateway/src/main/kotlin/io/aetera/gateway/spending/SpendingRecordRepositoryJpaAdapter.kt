package io.aetera.gateway.spending

import io.aetera.gateway.common.saveMerging
import io.aetera.model.spending.SpendingRecord
import io.aetera.model.spending.SpendingRecordRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class SpendingRecordRepositoryJpaAdapter(
    private val spendingRecordJpaRepository: SpendingRecordJpaRepository,
) : SpendingRecordRepository {
    override fun save(record: SpendingRecord): SpendingRecord = spendingRecordJpaRepository
        .saveMerging(
            id = record.id.value,
            update = { it.applyFrom(record) },
            create = { SpendingRecordJpaEntity.from(record) },
        ).toModel()

    override fun findAllByUserId(userId: UserId): List<SpendingRecord> = spendingRecordJpaRepository
        .findAllByUserIdOrderByMonthDesc(userId.value)
        .map { it.toModel() }

    override fun findByUserIdAndMonth(
        userId: UserId,
        month: LocalDate,
    ): SpendingRecord? = spendingRecordJpaRepository.findByUserIdAndMonth(userId.value, month)?.toModel()

    override fun findAllByUserIdAndMonthBetween(
        userId: UserId,
        from: LocalDate,
        to: LocalDate,
    ): List<SpendingRecord> = spendingRecordJpaRepository
        .findAllByUserIdAndMonthBetweenOrderByMonthDesc(userId.value, from, to)
        .map { it.toModel() }

    override fun deleteAllByUserId(userId: UserId) {
        spendingRecordJpaRepository.deleteAllByUserId(userId.value)
    }

    override fun deleteByUserIdAndMonth(
        userId: UserId,
        month: LocalDate,
    ) {
        spendingRecordJpaRepository.deleteByUserIdAndMonth(userId.value, month)
    }
}
