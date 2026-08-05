package io.aetera.usecase.auth

import io.aetera.model.auth.AccessTokenProvider
import io.aetera.model.auth.AuthErrorCode
import io.aetera.model.auth.IssuedAccessToken
import io.aetera.model.auth.OpaqueToken
import io.aetera.model.auth.RefreshToken
import io.aetera.model.auth.RefreshTokenId
import io.aetera.model.auth.RefreshTokenPolicy
import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.auth.RefreshTokenRevocation
import io.aetera.model.user.Email
import io.aetera.model.user.User
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.model.user.UserStatus
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class RefreshSessionServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val refreshTokenRepository = mockk<RefreshTokenRepository>()
        val userRepository = mockk<UserRepository>()
        val accessTokenProvider = mockk<AccessTokenProvider>()
        val sessionRevoker = mockk<SessionRevoker>(relaxed = true)

        fun serviceAt(instant: Instant): RefreshSessionService {
            val clock = Clock.fixed(instant, ZoneOffset.UTC)
            return RefreshSessionService(
                refreshTokenRepository,
                userRepository,
                SessionIssuer(accessTokenProvider, refreshTokenRepository, clock),
                sessionRevoker,
                clock,
            )
        }

        val hong =
            User.reconstitute(
                id = UserId.next(),
                email = Email("hong@example.com"),
                nickname = "홍길동",
                timezone = User.DEFAULT_TIMEZONE,
                status = UserStatus.ACTIVE,
                registeredAt = now.minusSeconds(86_400),
                withdrawnAt = null,
            )

        val raw = OpaqueToken.generate()

        fun storedToken(issuedAt: Instant = now.minusSeconds(3600)) = RefreshToken.issue(
            id = RefreshTokenId.next(),
            userId = hong.id,
            tokenHash = OpaqueToken.hash(raw),
            issuedAt = issuedAt,
            timeToLive = Duration.ofDays(14),
        )

        beforeTest {
            clearMocks(refreshTokenRepository, userRepository, accessTokenProvider, sessionRevoker)
            every { refreshTokenRepository.save(any()) } answers { firstArg() }
            every { refreshTokenRepository.markRotated(any(), any()) } returns true
            every { accessTokenProvider.issue(hong.id) } returns IssuedAccessToken("new-access", 900)
            every { userRepository.getById(hong.id) } returns hong
        }

        it("유효한 토큰이면 회전을 표시하고 새 쌍을 발급한다") {
            val stored = storedToken()
            every { refreshTokenRepository.getByTokenHash(OpaqueToken.hash(raw)) } returns stored

            val result = serviceAt(now).refresh(raw)

            verify(exactly = 1) { refreshTokenRepository.markRotated(stored.id, now) }
            result.accessToken shouldBe "new-access"
            result.refreshToken shouldNotBe raw
        }

        // 완전히 동시에 들어온 요청은 조건부 갱신에서 한쪽만 이긴다. 진 쪽을 실패로 처리하면
        // 탭을 여러 개 열어 둔 사용자가 브라우저 복원 때마다 로그아웃당한다.
        it("동시 경합에서 진 요청도 정상으로 보고 새 쌍을 발급한다") {
            val stored = storedToken()
            every { refreshTokenRepository.getByTokenHash(OpaqueToken.hash(raw)) } returns stored
            every { refreshTokenRepository.markRotated(stored.id, now) } returns false

            val result = serviceAt(now).refresh(raw)

            result.accessToken shouldBe "new-access"
            verify(exactly = 0) { sessionRevoker.revokeAllSessions(any(), any()) }
        }

        it("모르는 토큰은 거절한다") {
            every { refreshTokenRepository.getByTokenHash(any()) } returns null

            shouldThrow<CoreException> { serviceAt(now).refresh("unknown") }
                .errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        }

        it("유예를 벗어난 재사용은 탈취로 보고 그 사용자의 세션을 전부 끊는다") {
            val stored = storedToken()
            val revokedAt = now.minus(RefreshTokenPolicy.ROTATION_GRACE).minusSeconds(1)
            stored.revoke(revokedAt, RefreshTokenRevocation.ROTATED)
            every { refreshTokenRepository.getByTokenHash(OpaqueToken.hash(raw)) } returns stored

            shouldThrow<CoreException> { serviceAt(now).refresh(raw) }
                .errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
            verify(exactly = 1) { sessionRevoker.revokeAllSessions(hong.id, now) }
        }

        it("회전 직후 유예 안의 재사용은 정상 경합으로 보고 새 쌍을 발급한다") {
            val stored = storedToken()
            stored.revoke(now.minusSeconds(1), RefreshTokenRevocation.ROTATED)
            every { refreshTokenRepository.getByTokenHash(OpaqueToken.hash(raw)) } returns stored

            val result = serviceAt(now).refresh(raw)

            result.accessToken shouldBe "new-access"
            verify(exactly = 0) { sessionRevoker.revokeAllSessions(any(), any()) }
        }

        // 로그아웃이나 탈취 대응으로 이미 끊은 토큰의 재사용은 탈취의 증거가 아니다.
        // 여기서 다시 전체 폐기를 돌리면 뒤늦게 도착한 요청 하나가 사용자가 방금 만든 세션까지 끊는다.
        it("의도적으로 끊긴 토큰의 재사용은 조용히 거절만 하고 전체 폐기를 돌리지 않는다") {
            val stored = storedToken()
            stored.revoke(now.minusSeconds(1), RefreshTokenRevocation.REVOKED)
            every { refreshTokenRepository.getByTokenHash(OpaqueToken.hash(raw)) } returns stored

            shouldThrow<CoreException> { serviceAt(now).refresh(raw) }
                .errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
            verify(exactly = 0) { sessionRevoker.revokeAllSessions(any(), any()) }
        }

        it("만료된 토큰은 거절한다") {
            val stored = storedToken(issuedAt = now.minus(Duration.ofDays(30)))
            every { refreshTokenRepository.getByTokenHash(OpaqueToken.hash(raw)) } returns stored

            shouldThrow<CoreException> { serviceAt(now).refresh(raw) }
                .errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        }

        // 만료 검사를 재사용 검사보다 뒤에 두면, 오래전에 만료된 토큰 하나만 들고 있어도
        // 재생할 때마다 전체 폐기가 돌아 피해자가 세션을 유지할 수 없게 된다.
        it("만료된 폐기 토큰을 재생해도 전체 폐기를 돌리지 않는다") {
            val stored = storedToken(issuedAt = now.minus(Duration.ofDays(30)))
            stored.revoke(now.minus(Duration.ofDays(29)), RefreshTokenRevocation.ROTATED)
            every { refreshTokenRepository.getByTokenHash(OpaqueToken.hash(raw)) } returns stored

            shouldThrow<CoreException> { serviceAt(now).refresh(raw) }
                .errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
            verify(exactly = 0) { sessionRevoker.revokeAllSessions(any(), any()) }
        }
    })
