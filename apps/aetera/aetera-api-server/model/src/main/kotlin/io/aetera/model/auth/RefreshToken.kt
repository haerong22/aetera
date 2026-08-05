package io.aetera.model.auth

import io.aetera.model.user.UserId
import io.aetera.shared.error.ensure
import java.time.Duration
import java.time.Instant

/**
 * 리프레시 토큰. 원문은 클라이언트(httpOnly 쿠키)만 갖고, 서버는 SHA-256 해시만 저장한다.
 * 재발급 시 이전 토큰을 폐기하고 새 토큰을 발급한다(rotation) — 탈취된 토큰이
 * 한 번 쓰이면 그 뒤로는 무효가 된다.
 */
class RefreshToken private constructor(
    val id: RefreshTokenId,
    val userId: UserId,
    val tokenHash: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
    revokedAt: Instant?,
    revokedReason: RefreshTokenRevocation?,
) {
    var revokedAt: Instant? = revokedAt
        private set

    var revokedReason: RefreshTokenRevocation? = revokedReason
        private set

    fun isExpired(at: Instant): Boolean = !expiresAt.isAfter(at)

    /** 회전으로 교체된 토큰인가. 로그아웃·탈취 대응으로 끊긴 토큰과 구분한다. */
    val isRotated: Boolean get() = revokedReason == RefreshTokenRevocation.ROTATED

    /**
     * 폐기된 직후 아주 짧은 시간 안에 같은 토큰이 다시 온 것인가.
     *
     * 탭을 여러 개 열어 두면 각 탭이 같은 쿠키로 재발급을 시도한다. 한쪽이 먼저 회전시키면
     * 다른 쪽은 방금 폐기된 토큰을 들고 오게 되는데, 이건 탈취가 아니라 정상적인 경합이다.
     * 이 구간만 탈취 대응에서 제외한다 — 유예를 벗어난 재사용은 그대로 탈취로 본다.
     */
    fun isWithinRotationGrace(at: Instant): Boolean {
        val revoked = revokedAt ?: return false
        // 회전으로 죽은 토큰만 유예를 받는다. 로그아웃·탈취 대응으로 끊은 토큰은 즉시 무효다.
        if (!isRotated) return false
        return !at.isAfter(revoked.plus(RefreshTokenPolicy.ROTATION_GRACE))
    }

    fun revoke(
        at: Instant,
        reason: RefreshTokenRevocation,
    ) {
        if (revokedAt != null) return
        revokedAt = at
        revokedReason = reason
    }

    override fun equals(other: Any?): Boolean = this === other || (other is RefreshToken && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "RefreshToken(id=$id, userId=$userId)"

    companion object {
        fun issue(
            id: RefreshTokenId,
            userId: UserId,
            tokenHash: String,
            issuedAt: Instant,
            timeToLive: Duration,
        ): RefreshToken {
            ensure(!timeToLive.isNegative && !timeToLive.isZero, AuthErrorCode.INVALID_CREDENTIAL, "토큰 유효 기간이 올바르지 않습니다.")
            return RefreshToken(
                id = id,
                userId = userId,
                tokenHash = tokenHash,
                issuedAt = issuedAt,
                expiresAt = issuedAt.plus(timeToLive),
                revokedAt = null,
                revokedReason = null,
            )
        }

        fun reconstitute(
            id: RefreshTokenId,
            userId: UserId,
            tokenHash: String,
            issuedAt: Instant,
            expiresAt: Instant,
            revokedAt: Instant?,
            revokedReason: RefreshTokenRevocation?,
        ): RefreshToken = RefreshToken(id, userId, tokenHash, issuedAt, expiresAt, revokedAt, revokedReason)
    }
}
