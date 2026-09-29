package io.aetera.model.auth

import io.aetera.model.user.UserId
import io.aetera.shared.error.ensure
import java.time.Duration
import java.time.Instant

/**
 * 비밀번호 재설정 링크에 실리는 일회용 토큰.
 *
 * **원문은 메일에만 있고 서버는 SHA-256 해시만 저장한다** — 리프레시 토큰과 같은 원칙이다.
 * DB 가 유출돼도 남의 비밀번호를 바꿀 수 없다. 이 토큰은 지금 비밀번호를 묻지 않고 계정을
 * 넘겨주므로, 평문으로 두면 DB 한 벌이 곧 전 계정의 마스터키가 된다.
 *
 * **한 번만 쓴다.** 메일은 지워지지 않고 남아 있고, 전달되거나 백업으로 복제되기도 한다.
 * 다 쓴 링크가 계속 통하면 그 메일을 나중에 보는 사람이 계정에 들어간다.
 */
class PasswordResetToken private constructor(
    val id: PasswordResetTokenId,
    val userId: UserId,
    val tokenHash: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
    /**
     * 쓴 시각. **여기서 바꾸지 않는다.**
     *
     * 다 썼다는 표시는 [PasswordResetTokenRepository.markAllUsedByUserId] 가 한꺼번에 한다 —
     * 쓴 토큰 하나만이 아니라 그 사용자의 **남은 링크까지 같이** 죽어야 하기 때문이다.
     * 모델에 낱개로 표시하는 길을 같이 두면 다음 사람이 그쪽을 부르고, 그러면 벌크 갱신이
     * 영속성 컨텍스트를 비우면서 그 표시를 버린다 — 이 저장소에서 이미 두 번 겪은 함정이다.
     */
    val usedAt: Instant?,
) {
    fun isExpired(at: Instant): Boolean = !expiresAt.isAfter(at)

    val isUsed: Boolean get() = usedAt != null

    /** 지금 이 토큰으로 비밀번호를 바꿀 수 있는가. 만료와 재사용을 **한 답으로** 묶는다. */
    fun isUsable(at: Instant): Boolean = !isUsed && !isExpired(at)

    override fun equals(other: Any?): Boolean = this === other || (other is PasswordResetToken && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    /** 해시도 찍지 않는다 — 로그에 남으면 저장소를 유출한 것과 같아진다. */
    override fun toString(): String = "PasswordResetToken(id=$id, userId=$userId)"

    companion object {
        fun issue(
            id: PasswordResetTokenId,
            userId: UserId,
            tokenHash: String,
            issuedAt: Instant,
            timeToLive: Duration = PasswordResetPolicy.TIME_TO_LIVE,
        ): PasswordResetToken {
            ensure(
                !timeToLive.isNegative && !timeToLive.isZero,
                AuthErrorCode.INVALID_PASSWORD_RESET_TOKEN,
                "토큰 유효 기간이 올바르지 않습니다.",
            )
            return PasswordResetToken(
                id = id,
                userId = userId,
                tokenHash = tokenHash,
                issuedAt = issuedAt,
                expiresAt = issuedAt.plus(timeToLive),
                usedAt = null,
            )
        }

        fun reconstitute(
            id: PasswordResetTokenId,
            userId: UserId,
            tokenHash: String,
            issuedAt: Instant,
            expiresAt: Instant,
            usedAt: Instant?,
        ): PasswordResetToken = PasswordResetToken(id, userId, tokenHash, issuedAt, expiresAt, usedAt)
    }
}
