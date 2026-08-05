package io.aetera.usecase.auth.cmd

data class LoginCommand(
    val email: String,
    val password: String,
)
