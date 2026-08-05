package io.aetera.usecase.auth.cmd

data class SignUpCommand(
    val email: String,
    val nickname: String,
    val password: String,
)
