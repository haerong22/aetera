package io.aetera.model.goal

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class GoalErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    INVALID_TITLE(ErrorKind.INVALID_INPUT, ErrorCode.GOAL_BAND + 1, "목표 이름은 1~100자여야 합니다."),
    INVALID_TARGET(ErrorKind.INVALID_INPUT, ErrorCode.GOAL_BAND + 2, "목표치가 올바르지 않습니다."),
    INVALID_PROGRESS(ErrorKind.INVALID_INPUT, ErrorCode.GOAL_BAND + 3, "진행도가 올바르지 않습니다."),
    INVALID_UNIT(ErrorKind.INVALID_INPUT, ErrorCode.GOAL_BAND + 4, "단위가 너무 깁니다."),

    GOAL_NOT_FOUND(ErrorKind.NOT_FOUND, ErrorCode.GOAL_BAND + 1, "목표를 찾을 수 없습니다."),
}
