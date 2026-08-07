package io.aetera.controller.common

import io.aetera.shared.error.ErrorCode
import io.aetera.shared.error.ErrorKind
import org.springframework.http.HttpStatus

/**
 * 도메인의 [ErrorKind] 를 HTTP 로 번역하는 유일한 지점.
 *
 * `상세 응답 코드 = HTTP status(3자리) + 일련번호(4자리)` 조합도 여기서 만든다.
 * 덕분에 `model` 은 HTTP 를 전혀 몰라도 된다.
 */
object HttpErrorMapper {
    fun statusOf(kind: ErrorKind): HttpStatus = when (kind) {
        ErrorKind.INVALID_INPUT -> HttpStatus.BAD_REQUEST
        ErrorKind.UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED
        ErrorKind.FORBIDDEN -> HttpStatus.FORBIDDEN
        ErrorKind.NOT_FOUND -> HttpStatus.NOT_FOUND
        ErrorKind.CONFLICT -> HttpStatus.CONFLICT
        ErrorKind.INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR
    }

    fun codeOf(errorCode: ErrorCode): Int = statusOf(errorCode.kind).value() * 10_000 + errorCode.sequence
}
