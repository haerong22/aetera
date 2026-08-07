package io.aetera.controller.common

import io.aetera.model.auth.AuthErrorCode
import io.aetera.shared.error.CoreException
import io.aetera.usecase.module.ModuleAccessService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.UUID

/**
 * `/api/v1/modules/{module-id}/..` 전체를 지키는 단 하나의 가드.
 *
 * 사용자가 활성화하지 않은 모듈의 API 는 여기서 403(MODULE_NOT_ENABLED) 으로 잘린다.
 * 덕분에 **모듈 컨트롤러에는 활성화 검사 코드가 한 줄도 없다** — 모듈 계약의 핵심.
 */
@Component
class ModuleGuardInterceptor(
    private val moduleAccessService: ModuleAccessService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (HttpMethod.OPTIONS.matches(request.method)) return true

        val userId =
            request.getAttribute(AuthInterceptor.USER_ID_ATTRIBUTE) as? UUID
                ?: throw CoreException(AuthErrorCode.UNAUTHENTICATED)
        val moduleId =
            request.requestURI
                .substringAfter(MODULE_PATH_PREFIX, missingDelimiterValue = "")
                .substringBefore('/')
                .takeIf { it.isNotBlank() }
                ?: return true

        moduleAccessService.checkAccess(userId, moduleId)
        return true
    }

    companion object {
        const val MODULE_PATH_PREFIX: String = "/api/v1/modules/"
    }
}
