package io.aetera.controller.auth

import io.aetera.usecase.auth.cmd.SignUpCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpReq(
    @field:NotBlank
    @field:Email
    @field:Schema(example = "hong@example.com")
    val email: String,
    @field:NotBlank
    @field:Size(max = 30)
    @field:Schema(example = "홍길동")
    val nickname: String,
    @field:NotBlank
    @field:Size(min = 8, max = 64)
    @field:Schema(example = "password1234")
    val password: String,
) {
    fun toCommand(): SignUpCommand = SignUpCommand(
        email = email,
        nickname = nickname,
        password = password,
    )
}
