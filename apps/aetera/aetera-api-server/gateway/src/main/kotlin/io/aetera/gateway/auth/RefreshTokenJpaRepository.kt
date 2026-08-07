package io.aetera.gateway.auth

import io.aetera.model.auth.RefreshTokenRevocation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenJpaEntity, UUID> {
    fun findByTokenHash(tokenHash: String): RefreshTokenJpaEntity?

    /**
     * 아직 폐기되지 않았을 때만 회전 표시를 남긴다. 동시에 들어온 요청 중 하나만 1을 받는다.
     * 진 요청은 예외 대신 0 을 받으므로 정상 경합으로 이어서 처리할 수 있다.
     *
     * `update versioned` 여야 한다. 그냥 `update` 는 @Version 을 올리지 않아서, 이 갱신 전에
     * 엔티티를 읽어 둔 트랜잭션이 낡은 버전으로 덮어써도 충돌이 감지되지 않는다.
     */
    @Modifying(clearAutomatically = true)
    @Query(
        "update versioned RefreshTokenJpaEntity t set t.revokedAt = :at, t.revokedReason = :reason " +
            "where t.uid = :id and t.revokedAt is null",
    )
    fun markRotated(
        @Param("id") id: UUID,
        @Param("at") at: Instant,
        @Param("reason") reason: RefreshTokenRevocation,
    ): Int

    /**
     * 그 사용자의 토큰을 전부 REVOKED 로 만든다.
     *
     * **이미 폐기된 토큰까지 포함해야 한다.** 활성 토큰만 끊으면, 회전으로 죽었지만 아직 유예가
     * 남은 토큰이 사유를 ROTATED 로 유지한 채 살아남아 "부활 티켓"이 된다 — 전체 폐기 직후
     * 그 토큰을 재생하면 유예 판정을 통과해 새 세션이 발급되고, 대응이 통째로 무력해진다.
     * 폐기 시각은 원래 값을 보존하고 사유만 덮는다.
     */
    @Modifying(clearAutomatically = true)
    @Query(
        "update versioned RefreshTokenJpaEntity t set t.revokedAt = coalesce(t.revokedAt, :at), t.revokedReason = :reason " +
            "where t.userId = :userId",
    )
    fun revokeAllByUserId(
        @Param("userId") userId: UUID,
        @Param("at") at: Instant,
        @Param("reason") reason: RefreshTokenRevocation,
    ): Int
}
