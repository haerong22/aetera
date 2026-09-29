package io.aetera.gateway.mail

import io.aetera.model.mail.Mail
import io.aetera.model.mail.MailSender
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

/**
 * 메일을 실제로 보낸다.
 *
 * `aetera.mail.sender=email` 일 때만 뜬다. 기본은 로그 발송기라, 자격증명이 없는
 * 환경에서 이 클래스 때문에 서버가 못 뜨는 일은 없다.
 *
 * 반대로 **`email` 을 골라 놓고 `spring.mail.*` 이 없으면 서버가 아예 안 뜬다.** 일부러
 * 그렇게 둔다 — 보내겠다고 해 놓고 못 보내는 상태로 도는 것보다 그 자리에서 아는 편이 낫다.
 *
 * 제목과 본문은 만드는 쪽이 짓는다. 여기서 만들면 로그 발송기가 보여 주는 글과
 * 실제로 나가는 글이 갈린다.
 *
 * 실패하면 던진다. 다이제스트는 "보냈다" 표시를 미뤄 다음 시각에 다시 시도하고,
 * 비밀번호 재설정은 사용자에게 다시 요청하게 한다.
 */
@Component
@ConditionalOnProperty(name = ["aetera.mail.sender"], havingValue = "email")
class EmailMailSender(
    private val mailSender: JavaMailSender,
    @param:Value("\${aetera.mail.from}") private val from: String,
) : MailSender {
    override fun send(mail: Mail) {
        val message =
            SimpleMailMessage().apply {
                setFrom(from)
                setTo(mail.to.value)
                setSubject(mail.subject)
                setText(mail.body)
            }
        mailSender.send(message)
    }
}
