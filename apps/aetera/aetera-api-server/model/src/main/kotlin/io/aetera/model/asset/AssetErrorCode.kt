package io.aetera.model.asset

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class AssetErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    INVALID_NAME(ErrorKind.INVALID_INPUT, ErrorCode.ASSET_BAND + 1, "이름은 1~100자여야 합니다."),
    INVALID_AMOUNT(ErrorKind.INVALID_INPUT, ErrorCode.ASSET_BAND + 2, "금액이 올바르지 않습니다."),
    INVALID_MONTH(ErrorKind.INVALID_INPUT, ErrorCode.ASSET_BAND + 3, "기록할 달이 올바르지 않습니다."),
    TOO_MANY_ENTRIES(ErrorKind.INVALID_INPUT, ErrorCode.ASSET_BAND + 4, "한 달에 담을 수 있는 항목 수를 넘었습니다."),
}
