package io.aetera.controller.common

import java.time.Duration

/**
 * 한도를 넘었다. [GlobalExceptionHandler] 가 429 와 `Retry-After` 로 바꾼다.
 *
 * 응답을 인터셉터에서 직접 쓰지 않고 던지는 이유 — 예외를 응답으로 바꾸는 자리가
 * 한 곳이어야 `{code, message}` 모양이 갈라지지 않는다.
 *
 * 여기 적은 문장은 **로그와 스택트레이스용**이다. 사용자에게 나갈 문장은 핸들러가
 * [retryAfter] 로 짓는다 — 예외가 지은 것을 핸들러가 버리면, 읽는 사람은 이 문장이
 * 응답에 나가는 줄 오해한다.
 */
class RateLimitedException(
    val retryAfter: Duration,
) : RuntimeException("요청 한도 초과 — ${retryAfter.seconds}초 뒤 재시도")
