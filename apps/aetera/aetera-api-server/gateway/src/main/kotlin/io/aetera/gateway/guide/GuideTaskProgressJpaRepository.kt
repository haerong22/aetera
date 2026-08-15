package io.aetera.gateway.guide

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface GuideTaskProgressJpaRepository : JpaRepository<GuideTaskProgressJpaEntity, UUID> {
    /** 화면 한 번에 필요한 전부. 항목 수가 수십 개라 페이징 없이 통째로 읽는다. */
    @Query("select p from GuideTaskProgressJpaEntity p where p.journeyId = :journeyId")
    fun findAllByJourneyId(
        @Param("journeyId") journeyId: UUID,
    ): List<GuideTaskProgressJpaEntity>

    @Query(
        "select p from GuideTaskProgressJpaEntity p where p.journeyId = :journeyId and p.taskKey = :taskKey",
    )
    fun findByJourneyIdAndTaskKey(
        @Param("journeyId") journeyId: UUID,
        @Param("taskKey") taskKey: String,
    ): GuideTaskProgressJpaEntity?

    /**
     * 여정 초기화용 일괄 삭제. 영속성 컨텍스트에 남은 사본이 이어서 flush 되면 지운 행이
     * 되살아나므로 [Modifying.clearAutomatically] 로 비운다.
     */
    @Modifying(clearAutomatically = true)
    @Query("delete from GuideTaskProgressJpaEntity p where p.journeyId = :journeyId")
    fun deleteAllByJourneyId(
        @Param("journeyId") journeyId: UUID,
    ): Int
}
