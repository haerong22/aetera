package io.aetera.model.user

import io.aetera.shared.error.CoreException

/**
 * 이메일 주소. 저장·조회 모두 이 타입을 거치므로 정규화도 여기서 한다.
 *
 * 정규화하지 않으면 `Hong@Example.com` 과 `hong@example.com` 이 서로 다른 값이 되어
 * 같은 메일함으로 계정이 두 개 생기고, 대소문자를 다르게 입력한 사용자는 로그인에 실패한다
 * (`users.email` 유니크 인덱스는 대소문자를 구분한다).
 */
@JvmInline
value class Email private constructor(
    val value: String,
) {
    init {
        if (!PATTERN.matches(value)) {
            throw CoreException(UserErrorCode.INVALID_EMAIL, "'$value'는 올바른 이메일 형식이 아닙니다.")
        }
    }

    override fun toString(): String = value

    companion object {
        private val PATTERN = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        /**
         * `Email("...")` 호출은 전부 이 팩터리를 거친다(생성자는 private).
         * 앞뒤 공백을 지우고 소문자로 맞춘 뒤에야 값이 만들어진다.
         */
        operator fun invoke(raw: String): Email = Email(raw.trim().lowercase())
    }
}
