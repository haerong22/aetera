package io.aetera.model.auth

import java.time.Duration

object PasswordResetPolicy {
    /**
     * 링크가 살아 있는 시간.
     *
     * 짧게 둔다. 이 토큰 하나면 비밀번호를 갈아 끼울 수 있어 **비밀번호와 같은 힘**을 갖는데,
     * 메일함은 브라우저 세션보다 훨씬 오래 열려 있고 여러 기기에 동기화된다. 받은 편지함에
     * 남은 옛 링크가 몇 시간씩 유효하면, 메일함을 잠깐 들여다본 사람이 계정을 가져간다.
     *
     * 반대로 너무 짧으면 메일이 도착하기도 전에 죽는다. 30분은 그 사이다.
     */
    val TIME_TO_LIVE: Duration = Duration.ofMinutes(30)
}
