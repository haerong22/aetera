package io.aetera.usecase.auth

import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.user.UserId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

private val log = KotlinLogging.logger {}

/**
 * 한 사용자의 모든 세션을 끊는다.
 *
 * **반드시 별도 트랜잭션에서 커밋해야 한다.** 탈취를 감지한 쪽은 곧바로 [CoreException]
 * (RuntimeException)을 던지는데, 그러면 스프링 기본 롤백 정책이 같은 트랜잭션에 있던
 * 폐기 처리까지 되돌려 버린다 — 탈취를 감지하고도 대응은 취소되는 상태가 된다.
 * 그래서 이 빈을 분리해 `REQUIRES_NEW` 로 커밋을 보장한다.
 * (같은 클래스 안에서 부르면 프록시를 타지 않아 효과가 없다는 점도 분리 이유다.)
 */
@Component
class SessionRevoker(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun revokeAllSessions(
        userId: UserId,
        at: Instant,
    ) {
        refreshTokenRepository.revokeAllByUserId(userId, at)
        log.warn { "리프레시 토큰 재사용 감지 — 해당 사용자의 모든 세션을 끊었습니다. userId=$userId" }
    }
}
