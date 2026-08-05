package io.aetera.model.auth

import io.aetera.shared.error.CoreException

@JvmInline
value class EncryptedPassword(
    val value: String,
) {
    init {
        if (value.isBlank()) {
            throw CoreException(AuthErrorCode.INVALID_PASSWORD, "암호화된 비밀번호가 비어 있습니다.")
        }
    }

    override fun toString(): String = "EncryptedPassword(***)"
}
