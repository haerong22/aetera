package io.aetera.model.notification

import io.aetera.model.module.Notice
import io.aetera.model.user.Email
import java.time.LocalDate

/**
 * 한 사람에게 하루치로 묶어 보낼 것.
 *
 * 모듈마다 따로 보내지 않는다 — 만기 하나, 가이드 하나로 나뉘어 오면 받는 쪽에는
 * 그냥 메일이 여러 통일 뿐이고, 그러면 전부 읽지 않게 된다.
 *
 * 비어 있으면 아예 만들지 않는다. "오늘은 알릴 게 없습니다"를 매일 보내는 순간
 * 이 기능은 스팸이 된다.
 *
 * **글도 여기서 만든다.** 발송기마다 만들면 메일로 나가는 글과 로그에 찍히는 글이 달라지는데,
 * 로그 발송기가 있는 이유가 바로 "무엇이 나갈지 미리 보는 것"이라 그게 어긋나면 쓸모가 없다.
 */
data class NoticeDigest(
    val to: Email,
    val nickname: String,
    /** 받는 사람 기준의 날짜. 서버의 오늘이 아니다. */
    val on: LocalDate,
    /** 가까운 것부터. 이미 지난 만기가 맨 위에 온다. */
    val notices: List<Notice>,
) {
    init {
        require(notices.isNotEmpty()) { "빈 다이제스트는 만들지 않는다" }
    }

    /**
     * 받은 편지함에서 열어 보지 않고도 무엇인지 알게 한다.
     *
     * 한 건이면 그 건을 그대로 적는다 — "알림 1건"은 열어 봐야만 뜻이 생기는 제목이다.
     */
    val subject: String
        get() {
            val first = notices.first()
            val head = "${first.title}${first.detail?.let { " ($it)" } ?: ""}"
            val rest = notices.size - 1
            return if (rest > 0) "[아이테라] $head 외 ${rest}건" else "[아이테라] $head"
        }

    /** 글로 쓴다 — 항목 이름은 사용자가 적은 글자라 태그로 읽힐 여지를 두지 않는다. */
    val body: String
        get() =
            buildString {
                appendLine("${nickname}님, 오늘 챙길 것을 모았어요.")
                appendLine()
                notices.forEach { notice ->
                    appendLine("· ${formatDay(notice.on)}  ${notice.title}${notice.detail?.let { " — $it" } ?: ""}")
                }
                appendLine()
                append("알림을 그만 받으려면 아이테라 설정에서 끌 수 있어요.")
            }

    private fun formatDay(on: LocalDate): String = "${on.monthValue}월 ${on.dayOfMonth}일"
}
