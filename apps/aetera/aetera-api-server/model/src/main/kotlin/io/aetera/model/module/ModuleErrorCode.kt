package io.aetera.model.module

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class ModuleErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    INVALID_MODULE_ID(ErrorKind.INVALID_INPUT, ErrorCode.MODULE_BAND + 1, "모듈 아이디 형식이 올바르지 않습니다."),

    MODULE_NOT_ENABLED(ErrorKind.FORBIDDEN, ErrorCode.MODULE_BAND + 1, "사용 설정하지 않은 모듈입니다."),

    MODULE_NOT_FOUND(ErrorKind.NOT_FOUND, ErrorCode.MODULE_BAND + 1, "존재하지 않는 모듈입니다."),
}
