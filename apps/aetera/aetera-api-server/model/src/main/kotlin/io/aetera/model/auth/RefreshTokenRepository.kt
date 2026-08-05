package io.aetera.model.auth

import io.aetera.model.user.UserId
import java.time.Instant

interface RefreshTokenRepository {
    fun save(token: RefreshToken): RefreshToken

    fun getByTokenHash(tokenHash: String): RefreshToken?

    /**
     * 아직 폐기되지 않은 토큰에만 회전 표시를 남긴다. 표시에 성공하면 true.
     *
     * 탭 여러 개가 같은 쿠키로 동시에 재발급을 시도하면 모두 "아직 안 폐기됨"을 읽는다.
     * 각자 읽은 값으로 저장하면 낙관적 락이 충돌해 진 쪽이 409 를 받고, 프론트는 그걸
     * 세션 종료로 해석해 사용자를 로그아웃시킨다. 조건부 갱신 한 번으로 승패를 가려
     * 진 쪽도 정상 경합으로 처리할 수 있게 한다.
     */
    fun markRotated(
        id: RefreshTokenId,
        at: Instant,
    ): Boolean

    fun revokeAllByUserId(
        userId: UserId,
        at: Instant,
    )
}
