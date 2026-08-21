package io.aetera.gateway.renewal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface RenewalJpaRepository : JpaRepository<RenewalJpaEntity, UUID> {
    /** 만기가 같으면 id 로 순서를 고정한다 — tiebreaker 가 없으면 같은 요청이 매번 다른 순서를 준다. */
    @Query("select r from RenewalJpaEntity r where r.userId = :userId order by r.expiresAt asc, r.uid asc")
    fun findAllByUserId(
        @Param("userId") userId: UUID,
    ): List<RenewalJpaEntity>
}
