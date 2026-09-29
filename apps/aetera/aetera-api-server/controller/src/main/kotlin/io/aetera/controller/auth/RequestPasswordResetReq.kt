package io.aetera.controller.auth

import io.swagger.v3.oas.annotations.media.Schema

data class RequestPasswordResetReq(
    @field:Schema(example = "hong@example.com")
    val email: String,
)
