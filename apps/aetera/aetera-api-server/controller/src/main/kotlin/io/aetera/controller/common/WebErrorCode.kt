package io.aetera.controller.common

import org.springframework.http.HttpStatus

/**
 * HTTP 프로토콜 자체의 실패. 도메인과 무관하므로 인바운드 어댑터가 소유한다.
 *
 * 도메인 에러(`model` 의 `ErrorCode`)와 달리 HTTP 상태를 직접 들고 있다.
 * 여기는 이미 HTTP 를 아는 계층이라 [ErrorKind][io.aetera.shared.error.ErrorKind] 를 거칠 이유가 없다.
 *
 * 일련번호는 1~49 대역을 쓴다. 50 이상은 `model` 의 도메인 에러가 쓴다.
 */
enum class WebErrorCode(
    val status: HttpStatus,
    private val sequence: Int,
    val defaultMessage: String,
) {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 1, "요청 값 검증에 실패했습니다."),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, 2, "요청을 해석할 수 없습니다."),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, 1, "요청한 리소스를 찾을 수 없습니다."),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 1, "지원하지 않는 HTTP 메서드입니다."),

    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, 1, "다른 요청이 먼저 처리되었습니다. 다시 시도해 주세요."),

    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, 1, "지원하지 않는 요청 형식입니다."),

    /**
     * 도메인이 아니라 **전송 계층의 판단**이라 여기 있다. 무엇을 하려던 요청이든
     * "너무 잦다"는 답은 같고, 도메인은 이런 일이 있었는지도 모른다.
     */
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, 1, "요청이 너무 잦습니다. 잠시 후 다시 시도해 주세요."),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1, "일시적인 오류가 발생했습니다."),
    ;

    val code: Int get() = status.value() * 10_000 + sequence
}
