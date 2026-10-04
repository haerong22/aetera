package io.aetera.app

import io.aetera.model.mail.Mail
import io.aetera.model.mail.MailSender
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * 보낸 메일을 붙잡아 두는 발송기.
 *
 * 진짜 SMTP 없이 **나간 글 그대로** 볼 수 있다. 비밀번호 재설정은 링크가 본문에 실려 나가므로,
 * 사용자가 하는 일(메일을 열어 링크를 누르는 것)을 그대로 흉내 내려면 본문이 필요하다.
 *
 * ## 반드시 [reset] 으로 시작한다
 *
 * [AeteraIntegrationTest] 를 붙인 클래스들은 스프링 컨텍스트를 **돌려 쓰고**, 그러면 이 빈도
 * 클래스 사이에 공유된다. 앞 클래스가 남긴 메일이나 [broken] 이 그대로 보이므로, 이걸 읽는
 * 시험은 `@BeforeEach` 에서 비워야 한다.
 *
 * 쓰지 않는 시험은 신경 쓸 필요가 없다 — 남기는 것은 읽는 쪽이 치운다.
 */
class Outbox : MailSender {
    val sent = mutableListOf<Mail>()

    /** 켜면 발송이 터진다. SMTP 가 죽은 날을 흉내 낸다. */
    var broken = false

    override fun send(mail: Mail) {
        if (broken) throw IllegalStateException("SMTP 연결 실패")
        sent += mail
    }

    fun reset() {
        sent.clear()
        broken = false
    }

    /** 가장 나중에 보낸 메일 본문에서 재설정 링크의 토큰을 뽑는다. */
    fun resetTokenFromLastMail(): String = sent
        .last()
        .body
        .substringAfter("reset-password?token=")
        .substringBefore("\n")
        .trim()
}

@TestConfiguration
class TestMailbox {
    /**
     * `@Primary` 로 기본 발송기(로그)를 덮는다.
     *
     * 조건부 빈(`aetera.mail.sender`)을 속성으로 바꾸는 대신 이렇게 하는 이유 — 속성을 바꾸면
     * 메일 발송기가 뜨고 `spring.mail.*` 을 요구해 컨텍스트가 아예 못 뜬다.
     */
    @Bean
    @Primary
    fun outbox(): Outbox = Outbox()
}
