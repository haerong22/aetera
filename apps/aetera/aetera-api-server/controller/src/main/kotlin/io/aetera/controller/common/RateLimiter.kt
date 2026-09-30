package io.aetera.controller.common

import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * 토큰 버킷. 키 하나가 일정 시간에 쓸 수 있는 횟수를 제한한다.
 *
 * 고정 창(fixed window) 대신 버킷을 쓴다. 고정 창은 경계에서 두 배가 새어 나간다 —
 * 창이 끝나기 직전에 한도만큼, 시작하자마자 또 한도만큼 쓸 수 있다. 버킷은 쓴 만큼
 * 조금씩 차오르므로 그런 구멍이 없고, **잠깐의 몰림([capacity])은 허용하면서** 꾸준한
 * 남용만 막는다 — 오타로 몇 번 더 눌러 본 사람을 벌주지 않으려면 그쪽이 맞다.
 *
 * ## 이 구현의 한계
 *
 * **서버 한 대의 기억이다.** 인스턴스를 늘리면 한도도 대수만큼 늘어난다. 붙잡는 것은
 * "한 대에 쏟아붓는 것"까지이고, 여러 대에 나눠 때리는 것은 못 막는다. 제대로 하려면
 * 공용 저장소(레디스 등)가 필요한데, 지금 없는 인프라를 들이는 것보다 **없는 것보다는
 * 나은 방어**를 먼저 두는 쪽을 골랐다.
 *
 * 담아 둘 키 수에 상한([MAX_KEYS])을 둔다. 없으면 아이피를 바꿔 가며 때리는 것만으로
 * 메모리가 계속 는다 — 막으려던 것이 거꾸로 공격 수단이 된다. 넘치면 가장 오래 안 쓴
 * 키부터 버리는데, 그 키의 한도는 초기화된다. 활발한 키는 남으므로 실제 남용은 계속 걸린다.
 */
class RateLimiter(
    /** 한 번에 몰아 쓸 수 있는 횟수. */
    private val capacity: Int,
    /** 토큰 하나가 다시 차는 데 걸리는 시간. */
    private val refillInterval: Duration,
    private val clock: Clock,
) {
    init {
        require(capacity > 0) { "한도는 1 이상이어야 한다" }
        require(!refillInterval.isNegative && !refillInterval.isZero) { "충전 간격은 0보다 커야 한다" }
    }

    private class Bucket(
        var tokens: Double,
        var lastRefill: Instant,
    )

    /**
     * 접근 순서 [LinkedHashMap] 이라 가장 오래 안 쓴 키가 맨 앞에 온다.
     * 모든 접근을 [buckets] 에 건 락 안에서만 하므로 동기화는 이 한 곳으로 족하다.
     */
    private val buckets =
        object : LinkedHashMap<String, Bucket>(256, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<String, Bucket>): Boolean = size > MAX_KEYS
        }

    /**
     * 지금 기억하고 있는 키 수. **시험이 상한을 확인하려고 본다.**
     *
     * 이게 없으면 상한을 지우고 돌려도 시험이 전부 통과한다 — 실제로 그랬다.
     * 메모리가 무한정 느는 것을 막는 장치인데, 볼 수 없으면 아무도 지키지 않는다.
     */
    internal val keyCount: Int get() = synchronized(buckets) { buckets.size }

    /** 한 번 쓴다. 남은 것이 있으면 true, 없으면 false. */
    fun tryConsume(key: String): Boolean = synchronized(buckets) {
        val bucket = refreshed(key)
        if (bucket.tokens < 1.0) return false
        bucket.tokens -= 1.0
        true
    }

    /**
     * 다음 한 번까지 남은 시간. 지금 쓸 수 있으면 0이다.
     *
     * `Retry-After` 로 내보낸다 — 언제 다시 되는지 알려 주지 않으면 클라이언트가
     * 계속 두드리게 되고, 그건 막으려던 부하를 그대로 만든다.
     */
    fun retryAfter(key: String): Duration = synchronized(buckets) {
        val bucket = refreshed(key)
        if (bucket.tokens >= 1.0) return Duration.ZERO
        val missing = 1.0 - bucket.tokens
        Duration.ofMillis((missing * refillInterval.toMillis()).toLong().coerceAtLeast(1))
    }

    /** 흐른 시간만큼 채운 버킷. 없으면 가득 찬 채로 만든다. */
    private fun refreshed(key: String): Bucket {
        val now = clock.instant()
        val bucket = buckets.getOrPut(key) { Bucket(capacity.toDouble(), now) }

        val elapsed = Duration.between(bucket.lastRefill, now)
        if (elapsed.isNegative || elapsed.isZero) return bucket

        val gained = elapsed.toMillis().toDouble() / refillInterval.toMillis()
        bucket.tokens = (bucket.tokens + gained).coerceAtMost(capacity.toDouble())
        bucket.lastRefill = now
        return bucket
    }

    companion object {
        /**
         * 기억해 둘 키 수의 상한.
         *
         * 아이피와 이메일을 합쳐도 정상 사용에서는 한참 못 미친다. 넘어선다는 것은
         * 대개 누가 키를 바꿔 가며 때리고 있다는 뜻이다.
         */
        const val MAX_KEYS: Int = 10_000
    }
}
