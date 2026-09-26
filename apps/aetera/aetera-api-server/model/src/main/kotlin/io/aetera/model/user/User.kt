package io.aetera.model.user

import io.aetera.shared.error.CoreException
import io.aetera.shared.error.ensure
import java.time.Instant
import java.time.ZoneId

/**
 * 회원 프로필. 인증 수단(비밀번호, 소셜 연동)은 여기 두지 않고 `auth` 도메인의
 * [AuthCredential][io.aetera.model.auth.AuthCredential] 이 갖는다.
 * 카카오 로그인을 붙일 때 이 클래스는 한 줄도 바뀌지 않게 하기 위해서다.
 */
class User private constructor(
    val id: UserId,
    email: Email,
    nickname: String,
    timezone: ZoneId,
    status: UserStatus,
    val registeredAt: Instant,
) {
    var email: Email = email
        private set

    var nickname: String = nickname
        private set

    var timezone: ZoneId = timezone
        private set

    var status: UserStatus = status
        private set

    /**
     * 지금 쓸 수 있는 계정인가. 로그인·토큰 재발급·알림 발송이 이 값을 본다.
     *
     * 탈퇴는 상태를 바꾸지 않고 **행을 지운다**([UserRepository.delete]) — 남겨 두면
     * 이메일 유니크 인덱스가 그 주소를 묶는다. 그래서 지금 이 값은 언제나 true 이지만,
     * 정지(suspend) 같은 상태가 생기면 그 자리가 여기다.
     */
    val isActive: Boolean get() = status == UserStatus.ACTIVE

    fun changeNickname(newNickname: String) {
        requireActive()
        nickname = validateNickname(newNickname)
    }

    private fun requireActive() {
        ensure(
            isActive,
            UserErrorCode.USER_NOT_ACTIVE,
            "활성 상태의 사용자만 처리할 수 있습니다. 현재 상태: $status",
        )
    }

    override fun equals(other: Any?): Boolean = this === other || (other is User && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "User(id=$id, status=$status)"

    companion object {
        private const val NICKNAME_MAX_LENGTH = 30

        val DEFAULT_TIMEZONE: ZoneId = ZoneId.of("Asia/Seoul")

        fun register(
            id: UserId,
            email: Email,
            nickname: String,
            registeredAt: Instant,
            timezone: ZoneId = DEFAULT_TIMEZONE,
        ): User = User(
            id = id,
            email = email,
            nickname = validateNickname(nickname),
            timezone = timezone,
            status = UserStatus.ACTIVE,
            registeredAt = registeredAt,
        )

        fun reconstitute(
            id: UserId,
            email: Email,
            nickname: String,
            timezone: ZoneId,
            status: UserStatus,
            registeredAt: Instant,
        ): User = User(id, email, nickname, timezone, status, registeredAt)

        fun parseTimezone(value: String): ZoneId = runCatching { ZoneId.of(value) }.getOrElse {
            throw CoreException(UserErrorCode.INVALID_TIMEZONE, "'$value'는 올바른 타임존이 아닙니다.")
        }

        private fun validateNickname(nickname: String): String {
            val trimmed = nickname.trim()
            if (trimmed.isEmpty() || trimmed.length > NICKNAME_MAX_LENGTH) {
                throw CoreException(
                    UserErrorCode.INVALID_NICKNAME,
                    "닉네임은 1자 이상 ${NICKNAME_MAX_LENGTH}자 이하여야 합니다. 입력 길이: ${trimmed.length}",
                )
            }
            return trimmed
        }
    }
}
