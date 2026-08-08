package io.aetera.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Aetera API")
                .version("v1")
                .description(
                    "인생 전체를 다루는 모듈형 라이프 플랫폼. " +
                        "Swagger UI: /swagger-ui.html, OpenAPI 문서: /v3/api-docs",
                ),
        ).components(
            Components().addSecuritySchemes(
                BEARER_SCHEME,
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"),
            ),
        ).addSecurityItem(SecurityRequirement().addList(BEARER_SCHEME))

    companion object {
        private const val BEARER_SCHEME = "bearerAuth"
    }
}
