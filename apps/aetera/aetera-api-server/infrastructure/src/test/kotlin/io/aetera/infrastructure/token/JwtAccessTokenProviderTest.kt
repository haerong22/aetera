package io.aetera.infrastructure.token

import io.aetera.model.user.UserId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class JwtAccessTokenProviderTest :
    DescribeSpec({
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val secret = "test-secret-key-must-be-32-bytes!!"
        val ttl = Duration.ofMinutes(15)

        fun providerAt(instant: Instant) = JwtAccessTokenProvider(
            secret = secret,
            timeToLive = ttl,
            clock = Clock.fixed(instant, ZoneOffset.UTC),
        )

        it("발급한 토큰에서 사용자 아이디를 되찾는다") {
            val sut = providerAt(now)
            val userId = UserId.next()

            val issued = sut.issue(userId)

            issued.expiresInSeconds shouldBe ttl.seconds
            sut.verify(issued.token) shouldBe userId
        }

        it("만료된 토큰은 null 을 반환한다") {
            val userId = UserId.next()
            val issued = providerAt(now).issue(userId)

            providerAt(now.plus(Duration.ofMinutes(16))).verify(issued.token).shouldBeNull()
        }

        it("다른 키로 서명된 토큰은 거절한다") {
            val other =
                JwtAccessTokenProvider(
                    secret = "another-secret-key-32-bytes-long!!",
                    timeToLive = ttl,
                    clock = Clock.fixed(now, ZoneOffset.UTC),
                )
            val issued = other.issue(UserId.next())

            providerAt(now).verify(issued.token).shouldBeNull()
        }

        it("토큰이 아닌 문자열은 예외 없이 null 을 반환한다") {
            providerAt(now).verify("not-a-jwt").shouldBeNull()
        }

        it("짧은 비밀 키로는 기동을 거부한다") {
            shouldThrow<IllegalArgumentException> {
                JwtAccessTokenProvider("short", ttl, Clock.fixed(now, ZoneOffset.UTC))
            }
        }
    })
