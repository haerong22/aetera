package io.aetera.model.auth

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class AuthErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    INVALID_PASSWORD(ErrorKind.INVALID_INPUT, ErrorCode.AUTH_BAND + 1, "비밀번호 형식이 올바르지 않습니다."),
    INVALID_CREDENTIAL(ErrorKind.INVALID_INPUT, ErrorCode.AUTH_BAND + 2, "인증 수단이 올바르지 않습니다."),

    // 이메일 존재 여부를 구분해서 알려주면 가입 여부가 노출되므로 로그인 실패는 한 코드로 묶는다.
    LOGIN_FAILED(ErrorKind.UNAUTHENTICATED, ErrorCode.AUTH_BAND + 1, "이메일 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHENTICATED(ErrorKind.UNAUTHENTICATED, ErrorCode.AUTH_BAND + 2, "로그인이 필요합니다."),
    INVALID_REFRESH_TOKEN(ErrorKind.UNAUTHENTICATED, ErrorCode.AUTH_BAND + 3, "세션이 만료되었습니다. 다시 로그인해 주세요."),
}
