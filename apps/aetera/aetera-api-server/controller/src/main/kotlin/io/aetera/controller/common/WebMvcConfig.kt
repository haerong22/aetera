package io.aetera.controller.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 인바운드 어댑터의 웹 배선: 인증 인터셉터 → 모듈 가드 순서, 인증 사용자 리졸버, CORS.
 * 웹 기술의 상세라 config 모듈이 아니라 controller 가 갖는다.
 */
@Configuration(proxyBeanMethods = false)
class WebMvcConfig(
    private val authInterceptor: AuthInterceptor,
    private val moduleGuardInterceptor: ModuleGuardInterceptor,
    private val currentUserIdArgumentResolver: CurrentUserIdArgumentResolver,
    @Value("\${aetera.cors.allowed-origins:http://localhost:3000}") private val allowedOrigins: List<String>,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(authInterceptor)
            .order(1)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns(
                "/api/v1/auth/signup",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/auth/logout",
            )

        // 인증 인터셉터가 실어 둔 사용자 아이디에 의존하므로 반드시 그 뒤에 선다.
        // 경로는 가드가 들고 있는 상수를 그대로 쓴다 — 두 곳에 따로 적으면 어긋날 수 있다.
        registry
            .addInterceptor(moduleGuardInterceptor)
            .order(2)
            .addPathPatterns("${ModuleGuardInterceptor.MODULE_PATH_PREFIX}**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserIdArgumentResolver)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        // 리프레시 토큰이 httpOnly 쿠키로 오가므로 credentials 를 허용해야 한다.
        registry
            .addMapping("/api/**")
            .allowedOrigins(*allowedOrigins.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
