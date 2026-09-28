package io.aetera.usecase.auth.cmd

import java.util.UUID

data class ChangePasswordCommand(
    val userId: UUID,
    val currentPassword: String,
    val newPassword: String,
)
