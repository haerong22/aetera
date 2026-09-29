package io.aetera.controller.auth

import io.aetera.usecase.auth.cmd.ResetPasswordCommand
import io.swagger.v3.oas.annotations.media.Schema

data class ResetPasswordReq(
    @field:Schema(description = "메일 링크에 실려 온 토큰")
    val token: String,
    @field:Schema(example = "newpassword5678")
    val newPassword: String,
) {
    fun toCommand(): ResetPasswordCommand = ResetPasswordCommand(
        token = token,
        newPassword = newPassword,
    )
}
