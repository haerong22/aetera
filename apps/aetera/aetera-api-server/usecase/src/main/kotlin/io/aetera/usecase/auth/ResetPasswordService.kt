package io.aetera.usecase.auth

import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.AuthErrorCode
import io.aetera.model.auth.AuthProvider
import io.aetera.model.auth.OpaqueToken
import io.aetera.model.auth.PasswordEncryptor
import io.aetera.model.auth.PasswordPolicy
import io.aetera.model.auth.PasswordResetTokenRepository
import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.user.UserRepository
import io.aetera.shared.error.CoreException
import io.aetera.usecase.auth.cmd.ResetPasswordCommand
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

private val log = KotlinLogging.logger {}

/**
 * 링크로 받은 토큰으로 비밀번호를 다시 정한다.
 *
 * 지금 비밀번호를 묻지 않는다 — 모르는 사람이 쓰는 길이기 때문이다. 대신 토큰 하나가
 * 계정을 통째로 넘기므로, 지키는 것이 네 겹이다.
 *
 * 1. 해시로만 찾는다 — DB 를 봐도 링크를 만들 수 없다
 * 2. 30분 뒤 죽는다 — 메일함에 남은 옛 링크가 오래 살지 않는다
 * 3. 한 번만 쓴다, 그리고 **그 사용자의 다른 링크도 함께 죽는다**
 * 4. 세션을 전부 끊는다 — 이미 들어와 있던 사람을 내보낸다
 *
 * 4번이 특히 중요하다. 비밀번호를 잊어버린 것이 아니라 **빼앗긴** 것일 수 있는데,
 * 그때 세션을 남겨 두면 비밀번호만 바뀌고 침입자는 안에 그대로 있다.
 */
@Service
class ResetPasswordService(
    private val userRepository: UserRepository,
    private val authCredentialRepository: AuthCredentialRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncryptor: PasswordEncryptor,
    private val clock: Clock,
) {
    @Transactional
    fun reset(command: ResetPasswordCommand) {
        val now = clock.instant()
        val token =
            passwordResetTokenRepository.getByTokenHash(OpaqueToken.hash(command.token))
                ?: fail()
        // 만료와 재사용을 한 답으로 묶는다 — 나누면 주워 온 링크가 진짜였다는 사실이 새어 나간다.
        if (!token.isUsable(now)) fail()

        val user = userRepository.getById(token.userId) ?: fail()
        if (!user.isActive) fail()

        val credential =
            authCredentialRepository.getByUserIdAndProvider(user.id, AuthProvider.EMAIL) ?: fail()

        /*
         * 새 비밀번호 검사가 토큰 검사보다 **뒤에** 온다. 앞에 두면 아무 토큰이나 넣고
         * 규칙 위반 응답이 오는지로 토큰의 진위를 떠볼 수 있다.
         */
        PasswordPolicy.validate(command.newPassword)

        credential.changePassword(passwordEncryptor.encrypt(command.newPassword))
        authCredentialRepository.save(credential)

        /*
         * 쓴 표시가 먼저다. 이 갱신이 영속성 컨텍스트를 비우므로, 위의 저장이 아직
         * 표시로만 남아 있으면 통째로 버려진다 — 저장소 쪽에 `flushAutomatically` 를
         * 달아 두었지만, 순서로도 같은 것을 지킨다.
         *
         * 여러 번 요청해 링크가 여러 통 나가 있을 수 있다. 하나를 쓰면 나머지도 죽어야
         * 받은 편지함에 남은 다른 링크로 방금 정한 비밀번호를 또 바꾸는 일이 없다.
         */
        passwordResetTokenRepository.markAllUsedByUserId(user.id, now)
        refreshTokenRepository.revokeAllByUserId(user.id, now)

        log.info { "비밀번호 재설정 완료 — 모든 세션을 끊었습니다. userId=${user.id}" }
    }

    private fun fail(): Nothing = throw CoreException(AuthErrorCode.INVALID_PASSWORD_RESET_TOKEN)
}
