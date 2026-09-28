package io.aetera.usecase.auth

import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.AuthErrorCode
import io.aetera.model.auth.AuthProvider
import io.aetera.model.auth.PasswordEncryptor
import io.aetera.model.auth.PasswordPolicy
import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.shared.error.CoreException
import io.aetera.shared.error.ensure
import io.aetera.usecase.auth.cmd.ChangePasswordCommand
import io.aetera.usecase.user.getByIdOrThrow
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

private val log = KotlinLogging.logger {}

/**
 * 비밀번호 변경.
 *
 * **다른 기기를 전부 끊는다.** 비밀번호를 바꾸는 사람은 대개 남이 알아 버렸다고 의심하는
 * 중이다. 그런데 남이 이미 로그인해 두었다면 그 세션의 리프레시 토큰은 비밀번호와 무관하게
 * 살아 있어서, 비밀번호만 바꾸는 것은 **잠금장치를 바꾸고 침입자를 안에 두는 것**과 같다.
 *
 * 대신 이 기기의 세션은 새로 발급해 돌려준다. 안 그러면 비밀번호를 바꾸자마자
 * 자기도 로그아웃되는데, 그건 "제대로 된 건가" 싶은 경험이다.
 */
@Service
class ChangePasswordService(
    private val userRepository: UserRepository,
    private val authCredentialRepository: AuthCredentialRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncryptor: PasswordEncryptor,
    private val sessionIssuer: SessionIssuer,
    private val clock: Clock,
) {
    @Transactional
    fun changePassword(command: ChangePasswordCommand): AuthSessionDto {
        val user = userRepository.getByIdOrThrow(UserId(command.userId))
        val credential =
            authCredentialRepository.getByUserIdAndProvider(user.id, AuthProvider.EMAIL)
                ?: throw CoreException(AuthErrorCode.PASSWORD_LOGIN_NOT_AVAILABLE)
        val currentHash =
            credential.passwordHash ?: throw CoreException(AuthErrorCode.PASSWORD_LOGIN_NOT_AVAILABLE)

        /*
         * 지금 비밀번호를 다시 묻는다. 액세스 토큰만으로 바꿀 수 있으면, 잠깐 자리를 비운
         * 화면이나 훔친 토큰 하나로 계정을 통째로 가져갈 수 있다.
         */
        ensure(
            passwordEncryptor.matches(command.currentPassword, currentHash),
            AuthErrorCode.CURRENT_PASSWORD_MISMATCH,
        )
        PasswordPolicy.validate(command.newPassword)
        ensure(
            !passwordEncryptor.matches(command.newPassword, currentHash),
            AuthErrorCode.SAME_AS_CURRENT_PASSWORD,
        )

        credential.changePassword(passwordEncryptor.encrypt(command.newPassword))
        authCredentialRepository.save(credential)

        /*
         * 끊는 것이 먼저다. 발급을 먼저 하면 방금 만든 토큰까지 같이 끊긴다 —
         * 바꾸자마자 로그아웃되는 그 증상이 바로 이 순서를 뒤집었을 때 나온다.
         */
        refreshTokenRepository.revokeAllByUserId(user.id, clock.instant())
        log.info { "비밀번호 변경 완료 — 다른 세션을 모두 끊었습니다. userId=${user.id}" }
        return sessionIssuer.issue(user)
    }
}
