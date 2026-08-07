package io.aetera.controller.common

import io.aetera.shared.error.CoreException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

private val log = KotlinLogging.logger {}

/**
 * 예외를 `{code, message}` 응답으로 바꾸는 유일한 지점.
 *
 * 도메인이 던진 [CoreException] 은 [ErrorKind][io.aetera.shared.error.ErrorKind] 만 들고 오므로
 * 여기서 [HttpErrorMapper] 를 통해 HTTP 상태와 7자리 코드를 만든다.
 * 프로토콜 자체의 실패는 [WebErrorCode] 를 쓴다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CoreException::class)
    fun handleCoreException(e: CoreException): ResponseEntity<ErrorRes> {
        val status = HttpErrorMapper.statusOf(e.kind)
        val code = HttpErrorMapper.codeOf(e.errorCode)
        log.warn { "요청 거절 code=$code message=${e.message}" }
        return ResponseEntity.status(status).body(ErrorRes(code, e.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBodyValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorRes> {
        val detail =
            e.bindingResult.fieldErrors.joinToString(", ") {
                "${it.field}: ${it.defaultMessage ?: "올바르지 않은 값"}"
            }
        return webError(WebErrorCode.VALIDATION_FAILED, detail)
    }

    // @RequestParam 에 붙인 @Min/@Max 위반은 이쪽으로 온다 (@RequestBody 검증과 예외 타입이 다르다).
    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleParameterValidation(e: HandlerMethodValidationException): ResponseEntity<ErrorRes> {
        val detail =
            e.parameterValidationResults
                .flatMap { result ->
                    result.resolvableErrors.map {
                        "${result.methodParameter.parameterName}: ${it.defaultMessage ?: "올바르지 않은 값"}"
                    }
                }.joinToString(", ")
        return webError(WebErrorCode.VALIDATION_FAILED, detail)
    }

    @ExceptionHandler(
        HttpMessageNotReadableException::class,
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class,
    )
    fun handleMalformedRequest(e: Exception): ResponseEntity<ErrorRes> {
        log.warn { "잘못된 요청 형식: ${e.message}" }
        return webError(WebErrorCode.MALFORMED_REQUEST)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(e: NoResourceFoundException): ResponseEntity<ErrorRes> = webError(WebErrorCode.RESOURCE_NOT_FOUND)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorRes> =
        webError(WebErrorCode.METHOD_NOT_ALLOWED)

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(e: HttpMediaTypeNotSupportedException): ResponseEntity<ErrorRes> =
        webError(WebErrorCode.UNSUPPORTED_MEDIA_TYPE)

    // 동시에 같은 행을 고치면 @Version 이 충돌을 잡는다. 클라이언트가 재시도하면 되는 상황이라
    // 500 이 아니라 409 로 내려야 한다.
    @ExceptionHandler(OptimisticLockingFailureException::class, DataIntegrityViolationException::class)
    fun handleConcurrentModification(e: Exception): ResponseEntity<ErrorRes> {
        log.warn { "동시 수정 충돌: ${e.message}" }
        return webError(WebErrorCode.CONCURRENT_MODIFICATION)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ErrorRes> {
        log.error(e) { "처리되지 않은 예외" }
        return webError(WebErrorCode.INTERNAL_ERROR)
    }

    private fun webError(
        errorCode: WebErrorCode,
        message: String = errorCode.defaultMessage,
    ): ResponseEntity<ErrorRes> = ResponseEntity.status(errorCode.status).body(ErrorRes(errorCode.code, message))
}
