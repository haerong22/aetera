package io.aetera.gateway.guide

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface GuideJourneyJpaRepository : JpaRepository<GuideJourneyJpaEntity, UUID> {
    @Query("select j from GuideJourneyJpaEntity j where j.userId = :userId and j.guideId = :guideId")
    fun findByUserIdAndGuideId(
        @Param("userId") userId: UUID,
        @Param("guideId") guideId: String,
    ): GuideJourneyJpaEntity?
}
