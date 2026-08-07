package io.aetera.controller.common

import io.aetera.model.auth.AuthErrorCode
import io.aetera.shared.error.CoreException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.UUID

@Component
class CurrentUserIdArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.hasParameterAnnotation(CurrentUserId::class.java) &&
        parameter.parameterType == UUID::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): UUID {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
        return request?.getAttribute(AuthInterceptor.USER_ID_ATTRIBUTE) as? UUID
            ?: throw CoreException(AuthErrorCode.UNAUTHENTICATED)
    }
}
