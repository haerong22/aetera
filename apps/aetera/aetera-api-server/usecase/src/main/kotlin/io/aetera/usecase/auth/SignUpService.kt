package io.aetera.usecase.auth

import io.aetera.model.auth.AuthCredential
import io.aetera.model.auth.AuthCredentialId
import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.PasswordEncryptor
import io.aetera.model.auth.PasswordPolicy
import io.aetera.model.user.Email
import io.aetera.model.user.User
import io.aetera.model.user.UserErrorCode
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.shared.error.ensure
import io.aetera.usecase.auth.cmd.SignUpCommand
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

private val log = KotlinLogging.logger {}

@Service
class SignUpService(
    private val userRepository: UserRepository,
    private val authCredentialRepository: AuthCredentialRepository,
    private val passwordEncryptor: PasswordEncryptor,
    private val sessionIssuer: SessionIssuer,
    private val clock: Clock,
) {
    /** 가입 즉시 로그인 상태로 만든다 — 가입 후 로그인 화면으로 되돌리는 단절을 없앤다. */
    @Transactional
    fun signUp(command: SignUpCommand): AuthSessionDto {
        val email = Email(command.email)
        ensure(!userRepository.existsByEmail(email), UserErrorCode.EMAIL_ALREADY_REGISTERED)
        PasswordPolicy.validate(command.password)

        val now = clock.instant()
        val user =
            userRepository.save(
                User.register(
                    id = UserId.next(),
                    email = email,
                    nickname = command.nickname,
                    registeredAt = now,
                ),
            )
        authCredentialRepository.save(
            AuthCredential.email(
                id = AuthCredentialId.next(),
                userId = user.id,
                passwordHash = passwordEncryptor.encrypt(command.password),
                createdAt = now,
            ),
        )
        log.info { "사용자 가입 완료 id=${user.id}" }
        return sessionIssuer.issue(user)
    }
}
