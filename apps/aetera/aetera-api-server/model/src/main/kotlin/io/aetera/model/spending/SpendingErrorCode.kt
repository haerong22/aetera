package io.aetera.model.spending

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class SpendingErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    INVALID_AMOUNT(ErrorKind.INVALID_INPUT, ErrorCode.SPENDING_BAND + 1, "금액이 올바르지 않습니다."),
    INVALID_MONTH(ErrorKind.INVALID_INPUT, ErrorCode.SPENDING_BAND + 2, "기록할 달이 올바르지 않습니다."),
    INVALID_NOTE(ErrorKind.INVALID_INPUT, ErrorCode.SPENDING_BAND + 3, "메모는 200자 이하여야 합니다."),

    SPENDING_NOT_FOUND(ErrorKind.NOT_FOUND, ErrorCode.SPENDING_BAND + 1, "그 달의 기록을 찾을 수 없습니다."),
}
