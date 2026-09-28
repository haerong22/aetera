package io.aetera.controller.me

import io.aetera.usecase.auth.cmd.ChangePasswordCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class ChangePasswordReq(
    @field:Schema(example = "password1234")
    val currentPassword: String,
    @field:Schema(example = "newpassword5678")
    val newPassword: String,
) {
    fun toCommand(userId: UUID): ChangePasswordCommand = ChangePasswordCommand(
        userId = userId,
        currentPassword = currentPassword,
        newPassword = newPassword,
    )
}
