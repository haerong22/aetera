package io.aetera.usecase.auth

import io.aetera.model.auth.AuthCredential
import io.aetera.model.auth.AuthCredentialId
import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.AuthProvider
import io.aetera.model.auth.EncryptedPassword
import io.aetera.model.auth.OpaqueToken
import io.aetera.model.auth.PasswordResetToken
import io.aetera.model.auth.PasswordResetTokenRepository
import io.aetera.model.mail.Mail
import io.aetera.model.mail.MailSender
import io.aetera.model.user.Email
import io.aetera.model.user.User
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.model.user.UserStatus
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * 이 유스케이스가 지키는 것은 하나다 — **누가 가입했는지 알려 주지 않는 것.**
 *
 * 없는 주소와 있는 주소가 다르게 답하면 이 화면이 가입 여부 조회기가 된다. 주소록을 넣고
 * 돌리면 어느 주소가 이 서비스를 쓰는지 가려낼 수 있고, 그건 그 자체로 새면 안 되는 정보다.
 *
 * 그래서 "거절하는가"가 아니라 **"조용히 아무것도 안 하는가"** 를 본다.
 */
class RequestPasswordResetServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-09-29T00:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)

        val userRepository = mockk<UserRepository>()
        val authCredentialRepository = mockk<AuthCredentialRepository>()
        val tokenRepository = mockk<PasswordResetTokenRepository>(relaxed = true)
        val mailSender = mockk<MailSender>(relaxed = true)

        // 트랜잭션 경계는 여기서 볼 것이 아니다. 준 일을 그대로 실행해 준다.
        val transactionTemplate = mockk<TransactionTemplate>()

        val sut =
            RequestPasswordResetService(
                userRepository,
                authCredentialRepository,
                tokenRepository,
                mailSender,
                transactionTemplate,
                clock,
                "https://app.aetera.io",
            )

        val owner = UserId.next()
        val email = Email("hong@example.com")

        fun user(status: UserStatus = UserStatus.ACTIVE) = User.reconstitute(
            id = owner,
            email = email,
            nickname = "홍길동",
            timezone = User.DEFAULT_TIMEZONE,
            status = status,
            registeredAt = now.minusSeconds(86_400),
        )

        beforeTest {
            clearMocks(userRepository, authCredentialRepository, tokenRepository, mailSender, transactionTemplate)
            every { transactionTemplate.execute(any<TransactionCallback<Any>>()) } answers {
                firstArg<TransactionCallback<Any>>().doInTransaction(mockk(relaxed = true))
            }
            every { userRepository.getByEmail(email) } returns user()
            every { authCredentialRepository.getByUserIdAndProvider(owner, AuthProvider.EMAIL) } returns
                AuthCredential.email(AuthCredentialId.next(), owner, EncryptedPassword("해시"), now)
            every { tokenRepository.save(any()) } answers { firstArg() }
        }

        describe("보내기") {
            it("가입한 주소면 링크를 보낸다") {
                sut.request("hong@example.com")

                verify { mailSender.send(any()) }
            }

            it("토큰을 저장한다") {
                sut.request("hong@example.com")

                verify { tokenRepository.save(any()) }
            }

            /*
             * 원문은 메일에만 있어야 한다. 해시를 그대로 링크에 실으면 저장한 값과 보낸 값이
             * 같아져서, DB 를 본 사람이 곧바로 남의 계정에 들어갈 수 있다.
             */
            it("저장하는 것은 해시, 보내는 것은 원문") {
                val saved = slot<PasswordResetToken>()
                val sent = slot<Mail>()
                every { tokenRepository.save(capture(saved)) } answers { firstArg() }
                every { mailSender.send(capture(sent)) } returns Unit

                sut.request("hong@example.com")

                val link =
                    sent.captured.body
                        .substringAfter("token=")
                        .substringBefore("\n")
                link shouldNotBe saved.captured.tokenHash
                OpaqueToken.hash(link) shouldBe saved.captured.tokenHash
            }

            it("링크는 서버가 아는 주소로 만든다") {
                val sent = slot<Mail>()
                every { mailSender.send(capture(sent)) } returns Unit

                sut.request("hong@example.com")

                sent.captured.body shouldContain "https://app.aetera.io/reset-password?token="
            }

            // 요청한 적 없는 사람에게도 갈 수 있는 메일이다. 그 말이 없으면 받은 사람이 놀란다.
            it("요청한 적 없으면 무시하라고 적는다") {
                val sent = slot<Mail>()
                every { mailSender.send(capture(sent)) } returns Unit

                sut.request("hong@example.com")

                sent.captured.body shouldContain "요청한 적이 없다면"
            }

            it("메일에 비밀번호를 담지 않는다") {
                val sent = slot<Mail>()
                every { mailSender.send(capture(sent)) } returns Unit

                sut.request("hong@example.com")

                sent.captured.body shouldNotContain "해시"
            }
        }

        /*
         * 메일 서버가 흔들리는 동안 이 유스케이스가 **가입 여부 조회기**로 바뀌면 안 된다.
         * 가입한 주소만 예외를 던지면 컨트롤러가 500 을, 없는 주소는 204 를 내므로
         * 그 차이만으로 가려낼 수 있다 — 이 기능이 통째로 막으려던 그것이다.
         */
        describe("발송이 실패해도") {
            it("던지지 않는다") {
                every { mailSender.send(any()) } throws RuntimeException("SMTP 연결 실패")

                sut.request("hong@example.com")
            }

            // 없는 주소와 **똑같이** 끝나야 한다. 아래 "조용히 끝내기" 가 그 반대쪽이다.
            it("없는 주소와 같은 모양으로 끝난다") {
                every { mailSender.send(any()) } throws RuntimeException("SMTP 연결 실패")
                val withAccount = runCatching { sut.request("hong@example.com") }

                every { userRepository.getByEmail(email) } returns null
                val withoutAccount = runCatching { sut.request("hong@example.com") }

                withAccount.isSuccess shouldBe withoutAccount.isSuccess
            }
        }

        describe("조용히 끝내기") {
            /*
             * 이 넷은 모두 "보내지 않는다 + 던지지 않는다"여야 한다. 하나라도 예외를 던지면
             * 컨트롤러의 응답이 달라지고, 그 차이만으로 가입 여부를 읽어 낼 수 있다.
             */
            it("없는 주소면 아무것도 안 한다") {
                every { userRepository.getByEmail(email) } returns null

                sut.request("hong@example.com")

                verify(exactly = 0) { mailSender.send(any()) }
                verify(exactly = 0) { tokenRepository.save(any()) }
            }

            it("형식이 틀린 주소여도 던지지 않는다") {
                sut.request("주소가-아님")

                verify(exactly = 0) { mailSender.send(any()) }
            }

            it("빈 주소여도 던지지 않는다") {
                sut.request("")

                verify(exactly = 0) { mailSender.send(any()) }
            }

            /*
             * 소셜로만 가입한 계정에는 바꿀 비밀번호가 없다. 이 줄이 없으면 비밀번호가 없는
             * 계정에 비밀번호를 만들어 주게 되고, 그건 소셜 계정을 메일 한 통으로 빼앗는 길이다.
             */
            it("비밀번호 수단이 없는 계정에는 보내지 않는다") {
                every { authCredentialRepository.getByUserIdAndProvider(owner, AuthProvider.EMAIL) } returns null

                sut.request("hong@example.com")

                verify(exactly = 0) { mailSender.send(any()) }
                verify(exactly = 0) { tokenRepository.save(any()) }
            }
        }
    })
