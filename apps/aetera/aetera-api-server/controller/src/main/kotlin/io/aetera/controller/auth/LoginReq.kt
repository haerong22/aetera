package io.aetera.controller.auth

import io.aetera.usecase.auth.cmd.LoginCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginReq(
    @field:NotBlank
    @field:Email
    @field:Schema(example = "hong@example.com")
    val email: String,
    @field:NotBlank
    @field:Schema(example = "password1234")
    val password: String,
) {
    fun toCommand(): LoginCommand = LoginCommand(
        email = email,
        password = password,
    )
}
