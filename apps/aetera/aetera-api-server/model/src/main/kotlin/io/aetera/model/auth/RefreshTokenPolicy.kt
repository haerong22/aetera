package io.aetera.model.auth

import java.time.Duration

object RefreshTokenPolicy {
    val TIME_TO_LIVE: Duration = Duration.ofDays(14)

    /**
     * 회전 직후 같은 토큰의 재사용을 정상 경합으로 받아 주는 시간.
     *
     * 완전히 동시에 들어온 요청은 조건부 갱신([RefreshTokenRepository.markRotated])이 처리하므로,
     * 이 창은 "먼저 온 요청이 커밋된 뒤 몇 밀리초 늦게 도착한 요청"만 구제하면 된다.
     * 창이 열려 있는 동안은 탈취된 토큰도 통과하므로 짧게 유지한다.
     */
    val ROTATION_GRACE: Duration = Duration.ofSeconds(10)
}
