package io.aetera.model.expense

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class ExpenseErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    INVALID_TITLE(ErrorKind.INVALID_INPUT, ErrorCode.EXPENSE_BAND + 1, "이름은 1~100자여야 합니다."),
    INVALID_AMOUNT(ErrorKind.INVALID_INPUT, ErrorCode.EXPENSE_BAND + 2, "금액이 올바르지 않습니다."),
    MEMO_TOO_LONG(ErrorKind.INVALID_INPUT, ErrorCode.EXPENSE_BAND + 3, "메모가 너무 깁니다."),

    EXPENSE_NOT_FOUND(ErrorKind.NOT_FOUND, ErrorCode.EXPENSE_BAND + 1, "고정지출 항목을 찾을 수 없습니다."),
}
