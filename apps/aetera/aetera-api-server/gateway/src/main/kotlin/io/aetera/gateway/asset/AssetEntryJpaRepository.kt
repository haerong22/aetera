package io.aetera.gateway.asset

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface AssetEntryJpaRepository : JpaRepository<AssetEntryJpaEntity, UUID> {
    fun findAllByUserIdOrderByMonthDesc(userId: UUID): List<AssetEntryJpaEntity>

    fun deleteByUserIdAndMonth(
        userId: UUID,
        month: LocalDate,
    )
}
