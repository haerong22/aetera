package io.aetera.model.mail

import io.aetera.model.user.Email

/**
 * 보낼 메일 한 통.
 *
 * 무엇을 알리는 메일인지는 여기 없다 — 다이제스트든 비밀번호 재설정이든 **보내는 일은
 * 똑같기** 때문이다. 만드는 쪽이 제목과 본문을 다 지어서 건네고, 발송기는 그대로 내보낸다.
 *
 * 글을 발송기가 짓게 두지 않는 이유는 [NoticeDigest][io.aetera.model.notification.NoticeDigest]
 * 에 적은 것과 같다 — 로그 발송기가 보여 주는 글이 실제로 나갈 글과 달라지면 미리 볼 이유가 없다.
 */
data class Mail(
    val to: Email,
    val subject: String,
    val body: String,
) {
    init {
        require(subject.isNotBlank()) { "제목 없는 메일은 만들지 않는다" }
        require(body.isNotBlank()) { "본문 없는 메일은 만들지 않는다" }
    }
}
