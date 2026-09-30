package io.aetera.controller.common

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

/**
 * 한도 계산은 **시계를 손으로 돌려** 본다. 진짜로 기다리는 시험은 느리고, 느린 시험은
 * 결국 안 돌린다.
 */
class RateLimiterTest :
    DescribeSpec({
        val start = Instant.parse("2026-09-30T00:00:00Z")

        /** 원하는 만큼 시간을 밀 수 있는 시계. */
        class MovableClock : Clock() {
            var now: Instant = start

            fun advance(by: Duration) {
                now = now.plus(by)
            }

            override fun instant(): Instant = now

            override fun getZone(): java.time.ZoneId = ZoneOffset.UTC

            override fun withZone(zone: java.time.ZoneId?): Clock = this
        }

        describe("한도") {
            it("정해진 횟수까지는 통과한다") {
                val limiter = RateLimiter(3, Duration.ofMinutes(1), MovableClock())

                List(3) { limiter.tryConsume("a") } shouldBe listOf(true, true, true)
            }

            it("넘으면 막는다") {
                val limiter = RateLimiter(3, Duration.ofMinutes(1), MovableClock())
                repeat(3) { limiter.tryConsume("a") }

                limiter.tryConsume("a") shouldBe false
            }

            // 키를 안 나누면 한 사람이 쓴 한도에 다른 사람이 막힌다.
            it("키가 다르면 서로 영향이 없다") {
                val limiter = RateLimiter(1, Duration.ofMinutes(1), MovableClock())

                limiter.tryConsume("a") shouldBe true
                limiter.tryConsume("b") shouldBe true
                limiter.tryConsume("a") shouldBe false
            }
        }

        describe("다시 차오르기") {
            it("충전 간격이 지나면 한 번 더 쓸 수 있다") {
                val clock = MovableClock()
                val limiter = RateLimiter(2, Duration.ofMinutes(1), clock)
                repeat(2) { limiter.tryConsume("a") }

                clock.advance(Duration.ofMinutes(1))

                limiter.tryConsume("a") shouldBe true
                limiter.tryConsume("a") shouldBe false
            }

            /*
             * 고정 창이었다면 여기서 새어 나간다 — 창이 끝나기 직전에 한도만큼, 다음 창이
             * 시작하자마자 또 한도만큼 쓸 수 있어 짧은 순간에 두 배가 들어온다.
             * 버킷은 쓴 만큼 조금씩만 차므로 그 구멍이 없다.
             */
            it("창 경계에서 두 배가 새지 않는다") {
                val clock = MovableClock()
                val limiter = RateLimiter(5, Duration.ofMinutes(1), clock)
                repeat(5) { limiter.tryConsume("a") }

                // 창이 통째로 넘어갈 만한 시간을 밀어도, 그동안 찬 것은 한 개뿐이다.
                clock.advance(Duration.ofMinutes(1))

                List(5) { limiter.tryConsume("a") } shouldBe listOf(true, false, false, false, false)
            }

            it("오래 지나도 한도를 넘겨 쌓이지 않는다") {
                val clock = MovableClock()
                val limiter = RateLimiter(3, Duration.ofMinutes(1), clock)

                clock.advance(Duration.ofDays(7))

                List(4) { limiter.tryConsume("a") } shouldBe listOf(true, true, true, false)
            }
        }

        describe("다시 시도할 시각") {
            it("쓸 수 있으면 0이다") {
                val limiter = RateLimiter(1, Duration.ofMinutes(1), MovableClock())

                limiter.retryAfter("a") shouldBe Duration.ZERO
            }

            // 알려 주지 않으면 클라이언트가 계속 두드리고, 그건 막으려던 부하를 그대로 만든다.
            it("막혔으면 충전 간격 안쪽의 시간을 준다") {
                val limiter = RateLimiter(1, Duration.ofMinutes(1), MovableClock())
                limiter.tryConsume("a")

                val wait = limiter.retryAfter("a")

                wait shouldBe Duration.ofMinutes(1)
                wait shouldBeLessThanOrEqualTo Duration.ofMinutes(1)
            }

            it("절반쯤 찼으면 남은 만큼만 기다린다") {
                val clock = MovableClock()
                val limiter = RateLimiter(1, Duration.ofMinutes(1), clock)
                limiter.tryConsume("a")

                clock.advance(Duration.ofSeconds(40))

                limiter.retryAfter("a") shouldBe Duration.ofSeconds(20)
            }

            // 물어보는 것만으로 한도가 줄면, 안내하려다 사용자를 막게 된다.
            it("물어보는 것은 한도를 쓰지 않는다") {
                val limiter = RateLimiter(1, Duration.ofMinutes(1), MovableClock())

                repeat(5) { limiter.retryAfter("a") }

                limiter.tryConsume("a") shouldBe true
            }
        }

        describe("기억 상한") {
            /*
             * 상한이 없으면 아이피를 바꿔 가며 때리는 것만으로 메모리가 계속 는다 —
             * 막으려던 것이 거꾸로 공격 수단이 된다.
             *
             * 이 셋 중 앞의 둘이 **상한이 실제로 걸리는지**를 본다. 처음에는 "활발한 키가
             * 살아남는가"만 적었는데, 상한을 통째로 지우고 돌려도 통과했다 — 살아남는 것은
             * 상한이 없을 때 더 잘 되는 일이라 방어를 하나도 지키지 못하는 시험이었다.
             */
            it("키가 아무리 늘어도 상한을 넘겨 쌓지 않는다") {
                val limiter = RateLimiter(1, Duration.ofHours(1), MovableClock())

                repeat(RateLimiter.MAX_KEYS * 2) { limiter.tryConsume("flood-$it") }

                limiter.keyCount shouldBeLessThanOrEqualTo RateLimiter.MAX_KEYS
            }

            /*
             * 버려진 키는 한도가 초기화된다. 대가를 알고 고른 쪽이다 — 키를 흩어 때리는
             * 쪽은 제 한도도 같이 잃으므로 이득이 없고, 활발한 키는 남아 계속 걸린다.
             */
            it("오래 안 쓴 키는 버려져 한도가 초기화된다") {
                val limiter = RateLimiter(1, Duration.ofHours(1), MovableClock())
                limiter.tryConsume("old")
                limiter.tryConsume("old") shouldBe false

                // 'old' 를 한 번도 건드리지 않고 상한을 넘긴다.
                repeat(RateLimiter.MAX_KEYS + 10) { limiter.tryConsume("flood-$it") }

                limiter.tryConsume("old") shouldBe true
            }

            it("키가 넘쳐도 활발한 키의 한도는 지킨다") {
                val limiter = RateLimiter(1, Duration.ofHours(1), MovableClock())
                limiter.tryConsume("victim")

                // 상한을 넘기도록 다른 키를 쏟아붓되, 사이사이 victim 을 건드려 살려 둔다.
                repeat(RateLimiter.MAX_KEYS * 2) { index ->
                    limiter.tryConsume("flood-$index")
                    if (index % 100 == 0) limiter.retryAfter("victim")
                }

                limiter.tryConsume("victim") shouldBe false
            }
        }
    })
