package io.aetera.model.notification

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class NotificationErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    INVALID_SEND_HOUR(ErrorKind.INVALID_INPUT, ErrorCode.NOTIFICATION_BAND + 1, "받을 시각이 올바르지 않습니다."),
}
