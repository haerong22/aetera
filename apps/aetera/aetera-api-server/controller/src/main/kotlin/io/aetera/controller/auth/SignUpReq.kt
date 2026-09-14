package io.aetera.controller.auth

import io.aetera.usecase.auth.cmd.SignUpCommand
import io.swagger.v3.oas.annotations.media.Schema

data class SignUpReq(
    @field:Schema(example = "hong@example.com")
    val email: String,
    @field:Schema(example = "홍길동")
    val nickname: String,
    @field:Schema(example = "password1234")
    val password: String,
) {
    fun toCommand(): SignUpCommand = SignUpCommand(
        email = email,
        nickname = nickname,
        password = password,
    )
}
