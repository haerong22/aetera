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

    /**
     * 로그인 실패([LOGIN_FAILED])와 달리 **무엇이 틀렸는지 알려 준다.**
     * 이미 로그인한 사람이라 가입 여부가 새어 나갈 것이 없고, 숨기면 "새 비밀번호가
     * 규칙에 안 맞는 것"과 구분이 안 돼 사용자가 어디를 고쳐야 할지 모른다.
     */
    CURRENT_PASSWORD_MISMATCH(ErrorKind.INVALID_INPUT, ErrorCode.AUTH_BAND + 3, "현재 비밀번호가 올바르지 않습니다."),

    /**
     * 바꾸지 않은 것을 바꿨다고 답하지 않는다. 비밀번호를 바꾸는 사람은 대개
     * **남이 알아 버렸다고 의심하는 중**이라, 같은 값으로 끝나면 위험이 그대로인데
     * 처리된 줄 안다. 다른 기기를 끊는 일도 헛일이 된다.
     */
    SAME_AS_CURRENT_PASSWORD(ErrorKind.INVALID_INPUT, ErrorCode.AUTH_BAND + 4, "새 비밀번호가 지금 것과 같습니다."),

    // 이메일 존재 여부를 구분해서 알려주면 가입 여부가 노출되므로 로그인 실패는 한 코드로 묶는다.
    LOGIN_FAILED(ErrorKind.UNAUTHENTICATED, ErrorCode.AUTH_BAND + 1, "이메일 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHENTICATED(ErrorKind.UNAUTHENTICATED, ErrorCode.AUTH_BAND + 2, "로그인이 필요합니다."),
    INVALID_REFRESH_TOKEN(ErrorKind.UNAUTHENTICATED, ErrorCode.AUTH_BAND + 3, "세션이 만료되었습니다. 다시 로그인해 주세요."),

    /**
     * 없는 토큰·만료된 토큰·이미 쓴 토큰을 **한 코드로 묶는다.**
     *
     * 나누면 "이 토큰은 있지만 만료됐다"가 되어, 링크를 주워 온 사람에게 그 링크가
     * 진짜였다는 사실을 알려 준다. 사용자가 할 일은 어느 쪽이든 하나다 — 다시 요청하기.
     */
    INVALID_PASSWORD_RESET_TOKEN(
        ErrorKind.UNAUTHENTICATED,
        ErrorCode.AUTH_BAND + 4,
        "링크가 만료되었거나 이미 사용되었습니다. 다시 요청해 주세요.",
    ),

    /** 카카오 등으로만 가입한 계정. 지금은 이메일 가입뿐이라 닿지 않지만, 바꿀 자리가 없다는 사실은 지금도 참이다. */
    PASSWORD_LOGIN_NOT_AVAILABLE(ErrorKind.CONFLICT, ErrorCode.AUTH_BAND + 1, "비밀번호 로그인을 쓰지 않는 계정입니다."),
}
