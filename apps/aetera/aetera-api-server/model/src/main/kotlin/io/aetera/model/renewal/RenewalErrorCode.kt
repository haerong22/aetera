package io.aetera.model.renewal

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class RenewalErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    INVALID_TITLE(ErrorKind.INVALID_INPUT, ErrorCode.RENEWAL_BAND + 1, "이름은 1~100자여야 합니다."),
    INVALID_EXPIRY_DATE(ErrorKind.INVALID_INPUT, ErrorCode.RENEWAL_BAND + 2, "만기일이 올바르지 않습니다."),
    INVALID_NOTICE_DAYS(ErrorKind.INVALID_INPUT, ErrorCode.RENEWAL_BAND + 3, "미리 알림 일수가 올바르지 않습니다."),
    MEMO_TOO_LONG(ErrorKind.INVALID_INPUT, ErrorCode.RENEWAL_BAND + 4, "메모가 너무 깁니다."),
    CYCLE_NOT_REPEATABLE(ErrorKind.INVALID_INPUT, ErrorCode.RENEWAL_BAND + 5, "반복 주기가 없어 갱신할 수 없습니다."),

    RENEWAL_NOT_FOUND(ErrorKind.NOT_FOUND, ErrorCode.RENEWAL_BAND + 1, "만기 항목을 찾을 수 없습니다."),
}
