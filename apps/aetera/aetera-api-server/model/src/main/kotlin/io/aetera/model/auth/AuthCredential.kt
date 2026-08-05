package io.aetera.model.auth

import io.aetera.model.user.UserId
import io.aetera.shared.error.ensure
import java.time.Instant

/**
 * 사용자의 인증 수단 하나. 프로필([User][io.aetera.model.user.User])과 분리해서
 * 소셜 로그인 추가가 "행 하나 추가"로 끝나게 한다.
 *
 * - [AuthProvider.EMAIL]: [passwordHash] 필수, [providerUserId] 없음
 * - 소셜(KAKAO 등): [providerUserId] 필수, [passwordHash] 없음
 */
class AuthCredential private constructor(
    val id: AuthCredentialId,
    val userId: UserId,
    val provider: AuthProvider,
    val providerUserId: String?,
    val passwordHash: EncryptedPassword?,
    val createdAt: Instant,
) {
    init {
        when (provider) {
            AuthProvider.EMAIL -> {
                ensure(
                    passwordHash != null && providerUserId == null,
                    AuthErrorCode.INVALID_CREDENTIAL,
                    "이메일 인증 수단은 비밀번호 해시를 가져야 합니다.",
                )
            }

            else -> {
                ensure(
                    providerUserId != null && passwordHash == null,
                    AuthErrorCode.INVALID_CREDENTIAL,
                    "소셜 인증 수단은 제공자 사용자 아이디를 가져야 합니다.",
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean = this === other || (other is AuthCredential && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "AuthCredential(id=$id, provider=$provider)"

    companion object {
        fun email(
            id: AuthCredentialId,
            userId: UserId,
            passwordHash: EncryptedPassword,
            createdAt: Instant,
        ): AuthCredential = AuthCredential(
            id = id,
            userId = userId,
            provider = AuthProvider.EMAIL,
            providerUserId = null,
            passwordHash = passwordHash,
            createdAt = createdAt,
        )

        fun reconstitute(
            id: AuthCredentialId,
            userId: UserId,
            provider: AuthProvider,
            providerUserId: String?,
            passwordHash: EncryptedPassword?,
            createdAt: Instant,
        ): AuthCredential = AuthCredential(id, userId, provider, providerUserId, passwordHash, createdAt)
    }
}
