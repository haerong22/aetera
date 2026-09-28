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
    passwordHash: EncryptedPassword?,
    val createdAt: Instant,
) {
    var passwordHash: EncryptedPassword? = passwordHash
        private set

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

    /**
     * 비밀번호를 갈아 끼운다.
     *
     * 새 비밀번호가 규칙을 지키는지, 지금 것과 같지는 않은지는 **여기서 보지 않는다** —
     * 이 클래스는 해시만 알고 평문을 모르기 때문이다. 그 둘은 부르는 쪽
     * ([ChangePasswordService][io.aetera.usecase.auth.ChangePasswordService])의 몫이다.
     *
     * 소셜 계정에는 갈아 끼울 자리가 없다. 비밀번호 칸이 비어 있는데 채워 넣으면
     * [init] 이 지키던 "소셜은 비밀번호를 갖지 않는다"가 조용히 무너진다.
     */
    fun changePassword(newPasswordHash: EncryptedPassword) {
        ensure(
            provider == AuthProvider.EMAIL,
            AuthErrorCode.PASSWORD_LOGIN_NOT_AVAILABLE,
            "비밀번호를 쓰지 않는 인증 수단입니다. provider=$provider",
        )
        passwordHash = newPasswordHash
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
