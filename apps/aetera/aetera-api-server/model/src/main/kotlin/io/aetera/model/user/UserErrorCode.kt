package io.aetera.model.user

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class UserErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    INVALID_EMAIL(ErrorKind.INVALID_INPUT, ErrorCode.USER_BAND + 1, "이메일 형식이 올바르지 않습니다."),
    INVALID_NICKNAME(ErrorKind.INVALID_INPUT, ErrorCode.USER_BAND + 2, "닉네임은 1~30자여야 합니다."),
    INVALID_TIMEZONE(ErrorKind.INVALID_INPUT, ErrorCode.USER_BAND + 3, "타임존이 올바르지 않습니다."),

    USER_NOT_FOUND(ErrorKind.NOT_FOUND, ErrorCode.USER_BAND + 1, "사용자를 찾을 수 없습니다."),

    EMAIL_ALREADY_REGISTERED(ErrorKind.CONFLICT, ErrorCode.USER_BAND + 1, "이미 가입된 이메일입니다."),
    USER_ALREADY_WITHDRAWN(ErrorKind.CONFLICT, ErrorCode.USER_BAND + 2, "이미 탈퇴한 사용자입니다."),
}
