package io.aetera.model.auth

import io.aetera.shared.error.ensure

object PasswordPolicy {
    const val MIN_LENGTH: Int = 8
    const val MAX_LENGTH: Int = 64

    fun validate(rawPassword: String) {
        ensure(
            rawPassword.length in MIN_LENGTH..MAX_LENGTH,
            AuthErrorCode.INVALID_PASSWORD,
            "비밀번호는 ${MIN_LENGTH}자 이상 ${MAX_LENGTH}자 이하여야 합니다.",
        )
        ensure(
            rawPassword.any { it.isDigit() } && rawPassword.any { it.isLetter() },
            AuthErrorCode.INVALID_PASSWORD,
            "비밀번호는 영문과 숫자를 모두 포함해야 합니다.",
        )
    }
}
