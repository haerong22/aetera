package io.aetera.model.guide

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class GuideErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    // 할 일 키 형식 위반은 여기 없다 — 신뢰할 수 없는 입력은 GuideTaskKey.parseOrNull 이 걸러
    // TASK_NOT_FOUND 로 바뀌고, 그 밖의 경로는 우리 쪽 결함이라 GuideTaskKey 가 require 로 막는다.
    INVALID_ANCHOR_DATE(ErrorKind.INVALID_INPUT, ErrorCode.GUIDE_BAND + 1, "기준일이 올바르지 않습니다."),
    NOTE_TOO_LONG(ErrorKind.INVALID_INPUT, ErrorCode.GUIDE_BAND + 2, "메모가 너무 깁니다."),

    GUIDE_NOT_FOUND(ErrorKind.NOT_FOUND, ErrorCode.GUIDE_BAND + 1, "존재하지 않는 가이드입니다."),
    TASK_NOT_FOUND(ErrorKind.NOT_FOUND, ErrorCode.GUIDE_BAND + 2, "가이드에 없는 할 일입니다."),
    JOURNEY_NOT_STARTED(ErrorKind.NOT_FOUND, ErrorCode.GUIDE_BAND + 3, "아직 시작하지 않은 가이드입니다."),
}
