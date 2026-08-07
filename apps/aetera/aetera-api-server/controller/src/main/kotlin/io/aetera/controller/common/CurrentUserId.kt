package io.aetera.controller.common

/**
 * 인증된 사용자의 아이디를 컨트롤러 파라미터로 받는다.
 * 값은 [AuthInterceptor] 가 액세스 토큰을 검증해 요청에 실어 둔 것이다.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUserId
