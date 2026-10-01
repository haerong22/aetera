package io.aetera.gateway.spending

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID

interface SpendingRecordJpaRepository : JpaRepository<SpendingRecordJpaEntity, UUID> {
    fun findAllByUserIdOrderByMonthDesc(userId: UUID): List<SpendingRecordJpaEntity>

    fun findByUserIdAndMonth(
        userId: UUID,
        month: LocalDate,
    ): SpendingRecordJpaEntity?

    fun findAllByUserIdAndMonthBetweenOrderByMonthDesc(
        userId: UUID,
        from: LocalDate,
        to: LocalDate,
    ): List<SpendingRecordJpaEntity>

    /** 탈퇴할 때 한 번. 벌크 삭제인 이유는 다른 저장소에 적어 둔 것과 같다. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from SpendingRecordJpaEntity e where e.userId = :userId")
    fun deleteAllByUserId(
        @Param("userId") userId: UUID,
    )

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from SpendingRecordJpaEntity e where e.userId = :userId and e.month = :month")
    fun deleteByUserIdAndMonth(
        @Param("userId") userId: UUID,
        @Param("month") month: LocalDate,
    )
}
