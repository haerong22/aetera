package io.aetera.model.schedule

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind

enum class ScheduleErrorCode(
    override val kind: ErrorKind,
    override val sequence: Int,
    override val defaultMessage: String,
) : ErrorCode {
    INVALID_EVENT_TITLE(ErrorKind.INVALID_INPUT, ErrorCode.SCHEDULE_BAND + 1, "일정 제목은 1~200자여야 합니다."),
    INVALID_EVENT_PERIOD(ErrorKind.INVALID_INPUT, ErrorCode.SCHEDULE_BAND + 2, "일정 종료 시각은 시작 시각보다 빠를 수 없습니다."),
    INVALID_EVENT_COLOR(ErrorKind.INVALID_INPUT, ErrorCode.SCHEDULE_BAND + 3, "색상은 #RRGGBB 형식이어야 합니다."),
    INVALID_SEARCH_PERIOD(ErrorKind.INVALID_INPUT, ErrorCode.SCHEDULE_BAND + 4, "조회 기간이 올바르지 않습니다."),

    EVENT_NOT_FOUND(ErrorKind.NOT_FOUND, ErrorCode.SCHEDULE_BAND + 1, "일정을 찾을 수 없습니다."),
}
