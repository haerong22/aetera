package io.aetera.gateway.goal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface GoalJpaRepository : JpaRepository<GoalJpaEntity, UUID> {
    /**
     * 탈퇴할 때 한 번. **벌크 삭제**다 — 파생 삭제(`deleteAllByUserId`)는 엔티티를 읽어
     * 지움 표시만 해 두고 나중에 flush 하는데, 그 사이에 다른 기여자의
     * `@Modifying(clearAutomatically)` 가 영속성 컨텍스트를 비우면 그 표시가 통째로 버려진다.
     * 그러면 **빈 주입 순서에 따라 남는 데이터가 달라진다.**
     *
     * `flushAutomatically` 로 앞선 변경을 먼저 내보내고, `clearAutomatically` 로 뒤를 정리한다.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from GoalJpaEntity e where e.userId = :userId")
    fun deleteAllByUserId(
        @Param("userId") userId: UUID,
    )

    /** 만든 순서가 같으면 id 로 순서를 고정한다 — tiebreaker 가 없으면 매번 다른 순서가 나온다. */
    @Query("select g from GoalJpaEntity g where g.userId = :userId order by g.createdAt asc, g.uid asc")
    fun findAllByUserId(
        @Param("userId") userId: UUID,
    ): List<GoalJpaEntity>
}
