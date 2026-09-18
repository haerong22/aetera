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
 */
data class NoticeDigest(
    val to: Email,
    val nickname: String,
    /** 받는 사람 기준의 날짜. 서버의 오늘이 아니다. */
    val on: LocalDate,
    /** 가까운 것부터. 이미 지난 만기가 맨 위에 온다. */
    val notices: List<Notice>,
)
