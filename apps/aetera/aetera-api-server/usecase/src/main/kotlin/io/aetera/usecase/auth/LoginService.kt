package io.aetera.usecase.auth

import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.AuthErrorCode
import io.aetera.model.auth.AuthProvider
import io.aetera.model.auth.PasswordEncryptor
import io.aetera.model.user.Email
import io.aetera.model.user.UserRepository
import io.aetera.shared.error.CoreException
import io.aetera.usecase.auth.cmd.LoginCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginService(
    private val userRepository: UserRepository,
    private val authCredentialRepository: AuthCredentialRepository,
    private val passwordEncryptor: PasswordEncryptor,
    private val sessionIssuer: SessionIssuer,
) {
    /** 어떤 단계에서 실패했는지 구분하지 않는다 — 가입 여부가 응답으로 노출되면 안 된다. */
    @Transactional
    fun login(command: LoginCommand): AuthSessionDto {
        val user = userRepository.getByEmail(Email(command.email)) ?: failLogin()
        if (!user.isActive) failLogin()

        val credential =
            authCredentialRepository.getByUserIdAndProvider(user.id, AuthProvider.EMAIL) ?: failLogin()
        val passwordHash = credential.passwordHash ?: failLogin()
        if (!passwordEncryptor.matches(command.password, passwordHash)) failLogin()

        return sessionIssuer.issue(user)
    }

    private fun failLogin(): Nothing = throw CoreException(AuthErrorCode.LOGIN_FAILED)
}
