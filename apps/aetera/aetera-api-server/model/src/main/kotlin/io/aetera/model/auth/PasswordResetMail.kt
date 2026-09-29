package io.aetera.model.auth

import io.aetera.model.mail.Mail
import io.aetera.model.user.Email

/**
 * 재설정 링크를 담은 메일의 글.
 *
 * 발송기가 아니라 여기서 짓는다 — [NoticeDigest][io.aetera.model.notification.NoticeDigest]
 * 와 같은 이유다. 로그 발송기가 보여 주는 글이 실제로 나갈 글과 달라지면 미리 볼 이유가 없다.
 */
object PasswordResetMail {
    fun of(
        to: Email,
        nickname: String,
        link: String,
    ): Mail = Mail(
        to = to,
        subject = "[아이테라] 비밀번호 재설정",
        body =
            buildString {
                appendLine("${nickname}님, 비밀번호를 다시 정하시려면 아래 주소를 여세요.")
                appendLine()
                appendLine(link)
                appendLine()
                appendLine("${PasswordResetPolicy.TIME_TO_LIVE.toMinutes()}분 동안만 열리고, 한 번 쓰면 닫혀요.")
                appendLine()
                /*
                 * 요청한 적 없는 사람에게도 이 메일이 갈 수 있다 — 남이 그 주소를 적어 넣으면
                 * 그만이다. 그때 "무시하면 된다"고 말해 주지 않으면, 받은 사람은 계정이
                 * 털렸다고 여기고 놀란다. 실제로는 이 메일만으로는 아무 일도 일어나지 않는다.
                 */
                append("요청한 적이 없다면 이 메일을 무시하세요. 비밀번호는 그대로예요.")
            },
    )
}
