package io.aetera.gateway.goal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface GoalJpaRepository : JpaRepository<GoalJpaEntity, UUID> {
    /** 만든 순서가 같으면 id 로 순서를 고정한다 — tiebreaker 가 없으면 매번 다른 순서가 나온다. */
    @Query("select g from GoalJpaEntity g where g.userId = :userId order by g.createdAt asc, g.uid asc")
    fun findAllByUserId(
        @Param("userId") userId: UUID,
    ): List<GoalJpaEntity>
}
