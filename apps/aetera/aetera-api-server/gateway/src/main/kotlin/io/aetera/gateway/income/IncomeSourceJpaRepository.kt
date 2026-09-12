package io.aetera.gateway.income

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IncomeSourceJpaRepository : JpaRepository<IncomeSourceJpaEntity, UUID> {
    fun findAllByUserId(userId: UUID): List<IncomeSourceJpaEntity>
}
