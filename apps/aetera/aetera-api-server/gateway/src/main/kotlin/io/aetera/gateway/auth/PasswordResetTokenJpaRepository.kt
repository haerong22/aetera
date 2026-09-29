package io.aetera.gateway.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface PasswordResetTokenJpaRepository : JpaRepository<PasswordResetTokenJpaEntity, UUID> {
    fun findByTokenHash(tokenHash: String): PasswordResetTokenJpaEntity?

    /**
     * 아직 안 쓴 토큰을 한꺼번에 죽인다. 비밀번호가 바뀌는 순간 부른다.
     *
     * 이 파일의 벌크 갱신은 `flushAutomatically` 와 `clearAutomatically` 를 **둘 다** 갖는다.
     * 하나만 있으면 앞서 저장한 새 비밀번호 해시가 flush 되기 전에 영속성 컨텍스트가 비워져
     * 통째로 버려진다 — 같은 실수를 [RefreshTokenJpaRepository] 에서 이미 한 번 했다.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PasswordResetTokenJpaEntity t set t.usedAt = :at where t.userId = :userId and t.usedAt is null")
    fun markAllUsedByUserId(
        @Param("userId") userId: UUID,
        @Param("at") at: Instant,
    ): Int

    /** 탈퇴할 때 한 번. 벌크 삭제인 이유는 [RefreshTokenJpaRepository.deleteAllByUserId] 에 적어 뒀다. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PasswordResetTokenJpaEntity t where t.userId = :userId")
    fun deleteAllByUserId(
        @Param("userId") userId: UUID,
    )
}
