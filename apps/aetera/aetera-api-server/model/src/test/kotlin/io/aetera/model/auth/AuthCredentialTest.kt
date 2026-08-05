package io.aetera.model.auth

import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Duration
import java.time.Instant

class AuthCredentialTest :
    DescribeSpec({
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val hash = EncryptedPassword("pbkdf2-sha256:1000:c2FsdA==:aGFzaA==")

        describe("email credential") {
            it("비밀번호 해시를 가진 이메일 인증 수단을 만든다") {
                val sut = AuthCredential.email(AuthCredentialId.next(), UserId.next(), hash, now)

                sut.provider shouldBe AuthProvider.EMAIL
                sut.passwordHash shouldBe hash
            }

            it("소셜 인증 수단이 비밀번호를 가지면 거절한다") {
                shouldThrow<CoreException> {
                    AuthCredential.reconstitute(
                        id = AuthCredentialId.next(),
                        userId = UserId.next(),
                        provider = AuthProvider.KAKAO,
                        providerUserId = null,
                        passwordHash = hash,
                        createdAt = now,
                    )
                }.errorCode shouldBe AuthErrorCode.INVALID_CREDENTIAL
            }
        }

        describe("PasswordPolicy") {
            it("8자 미만은 거절한다") {
                shouldThrow<CoreException> { PasswordPolicy.validate("ab1234") }
                    .errorCode shouldBe AuthErrorCode.INVALID_PASSWORD
            }

            it("영문과 숫자를 모두 포함해야 한다") {
                shouldThrow<CoreException> { PasswordPolicy.validate("abcdefghij") }
                    .errorCode shouldBe AuthErrorCode.INVALID_PASSWORD
            }

            it("조건을 만족하면 통과한다") {
                PasswordPolicy.validate("password1234")
            }
        }

        describe("RefreshToken") {
            it("유효 기간 안에서는 활성이다") {
                val sut =
                    RefreshToken.issue(
                        id = RefreshTokenId.next(),
                        userId = UserId.next(),
                        tokenHash = OpaqueToken.hash(OpaqueToken.generate()),
                        issuedAt = now,
                        timeToLive = Duration.ofDays(14),
                    )

                sut.isExpired(now.plusSeconds(60)) shouldBe false
                sut.isExpired(now.plus(Duration.ofDays(15))) shouldBe true
            }

            it("폐기하면 즉시 비활성이 된다") {
                val sut =
                    RefreshToken.issue(
                        id = RefreshTokenId.next(),
                        userId = UserId.next(),
                        tokenHash = "hash",
                        issuedAt = now,
                        timeToLive = Duration.ofDays(14),
                    )

                sut.revoke(now.plusSeconds(10), RefreshTokenRevocation.REVOKED)

                sut.revokedAt shouldBe now.plusSeconds(10)
                sut.isRotated shouldBe false
            }
        }

        describe("OpaqueToken") {
            it("매번 다른 토큰을 만든다") {
                OpaqueToken.generate() shouldNotBe OpaqueToken.generate()
            }

            it("같은 원문은 같은 해시를 만들고 원문과 해시는 다르다") {
                val raw = OpaqueToken.generate()

                OpaqueToken.hash(raw) shouldBe OpaqueToken.hash(raw)
                OpaqueToken.hash(raw) shouldNotBe raw
            }
        }
    })
