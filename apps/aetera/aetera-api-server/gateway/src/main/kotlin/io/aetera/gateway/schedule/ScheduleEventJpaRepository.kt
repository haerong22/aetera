package io.aetera.gateway.schedule

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface ScheduleEventJpaRepository : JpaRepository<ScheduleEventJpaEntity, UUID> {
    /**
     * 탈퇴할 때 한 번. **벌크 삭제**다 — 파생 삭제(`deleteAllByUserId`)는 엔티티를 읽어
     * 지움 표시만 해 두고 나중에 flush 하는데, 그 사이에 다른 기여자의
     * `@Modifying(clearAutomatically)` 가 영속성 컨텍스트를 비우면 그 표시가 통째로 버려진다.
     * 그러면 **빈 주입 순서에 따라 남는 데이터가 달라진다.**
     *
     * `flushAutomatically` 로 앞선 변경을 먼저 내보내고, `clearAutomatically` 로 뒤를 정리한다.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ScheduleEventJpaEntity e where e.userId = :userId")
    fun deleteAllByUserId(
        @Param("userId") userId: UUID,
    )

    /** 내보내기가 쓴다. 기간이 없으므로 인덱스의 선두 컬럼만 타고 훑는다. */
    fun findAllByUserIdOrderByStartsAtAsc(userId: UUID): List<ScheduleEventJpaEntity>

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
