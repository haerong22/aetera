package io.aetera.usecase.auth.cmd

data class ResetPasswordCommand(
    val token: String,
    val newPassword: String,
)
