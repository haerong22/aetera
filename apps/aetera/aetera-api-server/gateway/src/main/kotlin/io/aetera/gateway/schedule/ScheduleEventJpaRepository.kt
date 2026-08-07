package io.aetera.gateway.schedule

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface ScheduleEventJpaRepository : JpaRepository<ScheduleEventJpaEntity, UUID> {
    /**
     * 기간과 겹치는 일정: `starts_at <= :to AND ends_at >= :from` (양 끝 포함).
     * 인덱스 `(user_id, starts_at)` 를 타고, 시작 시각이 같은 일정은 id 로 순서를 고정한다
     * (tiebreaker 가 없으면 같은 요청이 매번 다른 순서를 돌려줄 수 있다).
     *
     * 파생 쿼리 이름 대신 @Query 로 적는다 — 이름 기반은 파라미터를 위치로 바인딩해서,
     * `to`/`from` 순서를 "고쳐" 놓으면 조건이 뒤집히는데도 컴파일이 통과한다.
     */
    @Query(
        "select e from ScheduleEventJpaEntity e " +
            "where e.userId = :userId and e.startsAt <= :to and e.endsAt >= :from " +
            "order by e.startsAt asc, e.uid asc",
    )
    fun findAllOverlapping(
        @Param("userId") userId: UUID,
        @Param("from") from: Instant,
        @Param("to") to: Instant,
    ): List<ScheduleEventJpaEntity>
}
