package io.aetera.usecase.auth

import io.aetera.model.auth.AuthErrorCode
import io.aetera.model.auth.OpaqueToken
import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.user.UserRepository
import io.aetera.shared.error.CoreException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class RefreshSessionService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val sessionIssuer: SessionIssuer,
    private val sessionRevoker: SessionRevoker,
    private val clock: Clock,
) {
    /**
     * 리프레시 토큰을 회전시킨다: 이전 토큰을 폐기하고 새 쌍을 발급한다.
     *
     * 이미 폐기된 토큰이 다시 왔을 때의 판단이 이 서비스의 핵심이다.
     * - 회전으로 교체된 토큰을 유예 시간 안에 다시 쓴 것: 탭 여러 개의 정상 경합 → 새 쌍을 준다.
     * - 회전으로 교체된 토큰을 유예 밖에서 쓴 것: 누군가 소비된 토큰의 사본을 갖고 있다는 뜻
     *   → 탈취로 보고 그 사용자의 세션을 전부 끊는다.
     * - 로그아웃이나 탈취 대응으로 이미 끊은 토큰: 탈취의 증거가 아니라 늦게 도착한 요청일 수 있다
     *   → 조용히 거절만 한다. 여기서 다시 전체 폐기를 돌리면, 뒤늦게 도착한 요청 하나가
     *     사용자가 방금 새로 만든 세션까지 끊어 버린다.
     */
    @Transactional
    fun refresh(rawRefreshToken: String): AuthSessionDto {
        val now = clock.instant()
        val stored =
            refreshTokenRepository.getByTokenHash(OpaqueToken.hash(rawRefreshToken))
                ?: throw CoreException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        // 만료 검사를 먼저 한다. 뒤로 미루면 몇 년 전에 만료된 토큰 하나만 들고 있어도
        // 재생할 때마다 전체 폐기가 돌아, 피해자가 세션을 유지하지 못하게 만들 수 있다.
        if (stored.isExpired(now)) throw CoreException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        val isReuse = stored.revokedAt != null
        if (isReuse) {
            if (!stored.isRotated) throw CoreException(AuthErrorCode.INVALID_REFRESH_TOKEN)
            if (!stored.isWithinRotationGrace(now)) {
                // 이 메서드는 바로 아래에서 예외를 던져 롤백되므로, 폐기는 별도 트랜잭션에서 커밋해야 한다.
                sessionRevoker.revokeAllSessions(stored.userId, now)
                throw CoreException(AuthErrorCode.INVALID_REFRESH_TOKEN)
            }
        }

        // 사용자 행이 사라진 경우까지 401 로 묶는다. 여기서 404 USER_NOT_FOUND 가 나가면
        // 토큰은 유효했고 계정만 없어졌다는 사실이 새고, 401 로 재시도를 판단하는 클라이언트도 어긋난다.
        val user = userRepository.getById(stored.userId) ?: throw CoreException(AuthErrorCode.INVALID_REFRESH_TOKEN)
        if (!user.isActive) throw CoreException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        // 아직 살아 있는 토큰이면 조건부 갱신으로 회전을 표시한다. 같은 순간 다른 탭이
        // 먼저 회전시켰다면 false 가 오는데, 그것도 정상 경합이므로 그대로 새 쌍을 발급한다.
        if (!isReuse) {
            refreshTokenRepository.markRotated(stored.id, now)
        }
        return sessionIssuer.issue(user)
    }
}
