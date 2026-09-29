package io.aetera.model.auth

import io.aetera.model.user.UserId
import java.time.Instant

interface PasswordResetTokenRepository {
    fun save(token: PasswordResetToken): PasswordResetToken

    fun getByTokenHash(tokenHash: String): PasswordResetToken?

    /**
     * 이 사용자의 아직 안 쓴 토큰을 모두 쓴 것으로 표시한다.
     *
     * 비밀번호가 바뀌는 순간 부른다. 여러 번 요청해 링크가 여러 통 나가 있을 수 있는데,
     * 하나로 바꾸고 나면 **나머지도 같이 죽어야 한다** — 안 그러면 받은 편지함에 남은
     * 다른 링크로 방금 정한 비밀번호를 또 바꿀 수 있다.
     */
    fun markAllUsedByUserId(
        userId: UserId,
        at: Instant,
    )

    /** 탈퇴할 때 한 번. 이 사용자의 것을 통째로 지운다. */
    fun deleteAllByUserId(userId: UserId)
}
