package io.aetera.usecase.auth

import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.AuthProvider
import io.aetera.model.auth.OpaqueToken
import io.aetera.model.auth.PasswordResetMail
import io.aetera.model.auth.PasswordResetToken
import io.aetera.model.auth.PasswordResetTokenId
import io.aetera.model.auth.PasswordResetTokenRepository
import io.aetera.model.mail.Mail
import io.aetera.model.mail.MailSender
import io.aetera.model.user.Email
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Clock

private val log = KotlinLogging.logger {}

/**
 * 비밀번호 재설정 요청 — 메일로 링크를 보낸다.
 *
 * **누가 가입했는지 알려 주지 않는다.** 없는 주소로 요청해도 똑같이 성공으로 답한다.
 * 구분해서 답하면 이 화면이 **가입 여부 조회기**가 된다 — 주소록을 넣고 돌리면 어느 주소가
 * 이 서비스를 쓰는지 가려낼 수 있다. 로그인 실패를 한 코드로 묶는 것과 같은 이유다.
 *
 * 그래서 이 유스케이스는 **아무것도 던지지 않는다.** 못 보낼 이유를 만나면 조용히 끝낸다.
 */
@Service
class RequestPasswordResetService(
    private val userRepository: UserRepository,
    private val authCredentialRepository: AuthCredentialRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val mailSender: MailSender,
    private val transactionTemplate: TransactionTemplate,
    private val clock: Clock,
    @param:Value("\${aetera.web.base-url}") private val webBaseUrl: String,
) {
    fun request(rawEmail: String) {
        val issued = transactionTemplate.execute { issueFor(rawEmail) } ?: return

        /*
         * 메일은 트랜잭션 **밖에서** 보낸다. 안에서 보내면 SMTP 가 느린 날 DB 커넥션을
         * 그만큼 붙들고 있게 되고, 보낸 뒤 커밋이 실패하면 **받은 링크가 서버에 없는** 상태가
         * 된다 — 사용자는 방금 받은 링크를 눌렀는데 만료됐다는 말을 듣는다.
         *
         * 실패를 **삼킨다.** 여기서 던지면 가입한 주소는 500, 없는 주소는 204 가 되어
         * 메일 서버가 흔들리는 동안 이 주소가 **가입 여부 조회기**로 바뀐다 —
         * 이 유스케이스가 통째로 막으려던 그것이다.
         *
         * 대가는 못 간 메일을 기다리게 되는 것인데, 화면에 다시 받는 길이 있고
         * 토큰은 30분 뒤 알아서 죽는다. 새는 쪽보다 낫다.
         */
        runCatching { mailSender.send(issued.mail) }
            .onSuccess { log.info { "비밀번호 재설정 링크 발송 userId=${issued.userId}" } }
            .onFailure { log.error(it) { "비밀번호 재설정 메일 발송 실패 userId=${issued.userId}" } }
    }

    /** 보낼 것이 있으면 그 한 통을, 없으면 null. **없는 이유는 남기지 않는다.** */
    private fun issueFor(rawEmail: String): Issued? {
        val email = runCatching { Email(rawEmail) }.getOrNull() ?: return null
        val user = userRepository.getByEmail(email) ?: return null
        if (!user.isActive) return null

        /*
         * 비밀번호로 로그인하지 않는 계정(카카오 등)에는 보낼 것이 없다. 지금은 이메일
         * 가입뿐이라 닿지 않지만, 소셜을 붙이는 날 이 줄이 없으면 비밀번호가 없는 계정에
         * 비밀번호를 만들어 주는 길이 열린다 — 소셜 계정을 메일 한 통으로 빼앗는 경로다.
         */
        authCredentialRepository.getByUserIdAndProvider(user.id, AuthProvider.EMAIL) ?: return null

        val rawToken = OpaqueToken.generate()
        passwordResetTokenRepository.save(
            PasswordResetToken.issue(
                id = PasswordResetTokenId.next(),
                userId = user.id,
                tokenHash = OpaqueToken.hash(rawToken),
                issuedAt = clock.instant(),
            ),
        )
        return Issued(user.id, PasswordResetMail.of(user.email, user.nickname, linkFor(rawToken)))
    }

    /**
     * 주소는 **서버가 아는 값**으로만 만든다. 요청 헤더(Host·Origin)에서 가져오면 아무나
     * 보낸 헤더로 링크의 목적지를 바꿀 수 있고, 그러면 남의 재설정 토큰이 공격자 사이트로
     * 걸어 들어간다.
     */
    private fun linkFor(rawToken: String): String {
        val encoded = URLEncoder.encode(rawToken, StandardCharsets.UTF_8)
        return "${webBaseUrl.trimEnd('/')}/reset-password?token=$encoded"
    }

    private data class Issued(
        val userId: UserId,
        val mail: Mail,
    )
}
