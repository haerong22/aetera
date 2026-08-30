package io.aetera.gateway.expense

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FixedExpenseJpaRepository : JpaRepository<FixedExpenseJpaEntity, UUID> {
    fun findAllByUserIdOrderByCreatedAtAsc(userId: UUID): List<FixedExpenseJpaEntity>
}
