package io.aetera.model.timeline

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class TimelineErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    INVALID_RANGE(ErrorKind.INVALID_INPUT, ErrorCode.TIMELINE_BAND + 1, "조회 기간이 올바르지 않습니다."),
}
