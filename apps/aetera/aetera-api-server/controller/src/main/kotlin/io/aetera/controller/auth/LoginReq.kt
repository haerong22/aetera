package io.aetera.controller.auth

import io.aetera.usecase.auth.cmd.LoginCommand
import io.swagger.v3.oas.annotations.media.Schema

data class LoginReq(
    @field:Schema(example = "hong@example.com")
    val email: String,
    @field:Schema(example = "password1234")
    val password: String,
) {
    fun toCommand(): LoginCommand = LoginCommand(
        email = email,
        password = password,
    )
}
