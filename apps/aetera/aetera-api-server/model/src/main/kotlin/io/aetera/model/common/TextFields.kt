package io.aetera.model.common

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ensure

/*
 * 글자 칸의 공통 규칙. 여섯 모델이 같은 열 줄을 각자 갖고 있던 것을 여기로 모았다.
 *
 * 상한과 오류 코드는 부르는 쪽이 정한다. 일정 제목은 200자이고 나머지는 100자이며,
 * 어긋났을 때 어떤 코드로 답할지는 도메인의 몫이다. 여기서 정하면 모듈이 공용 코드에
 * 묶여 버린다 — 공통인 것은 "털고, 재고, 막는다"는 절차뿐이다.
 */

/** 반드시 있어야 하는 칸. 앞뒤 공백을 턴 뒤 1자 이상 [max] 자 이하인지 본다. */
fun requiredText(
    value: String,
    max: Int,
    errorCode: ErrorCode,
    label: String,
): String {
    val trimmed = value.trim()
    ensure(
        trimmed.isNotEmpty() && trimmed.length <= max,
        errorCode,
        "$label${topicParticle(label)} 1자 이상 ${max}자 이하여야 합니다. 입력 길이: ${trimmed.length}",
    )
    return trimmed
}

/**
 * 있어도 되고 없어도 되는 칸.
 *
 * 공백만 친 것은 `null` 로 접는다 — 안 쓴 것과 다르게 저장하면 화면이 빈 줄을 그린다.
 */
fun optionalText(
    value: String?,
    max: Int,
    errorCode: ErrorCode,
    label: String,
): String? {
    val trimmed = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    ensure(
        trimmed.length <= max,
        errorCode,
        "$label${topicParticle(label)} ${max}자 이하여야 합니다. 입력 길이: ${trimmed.length}",
    )
    return trimmed
}

/**
 * 받침이 있으면 "은", 없으면 "는". 한글 음절은 `(코드 − '가') % 28` 이 0일 때 받침이 없다.
 *
 * 문법 욕심이 아니라 이 함수가 여러 도메인에서 불리기 때문이다. 조사를 고정하면
 * "메모은" 같은 말이 나가고, 부르는 쪽마다 조사를 적어 넣게 하면 그게 또 새 중복이다.
 */
private fun topicParticle(label: String): String {
    val last = label.lastOrNull() ?: return "은"
    if (last !in '가'..'힣') return "은"
    return if ((last - '가') % 28 == 0) "는" else "은"
}
