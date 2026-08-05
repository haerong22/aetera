package io.aetera.model.auth

interface PasswordEncryptor {
    fun encrypt(rawPassword: String): EncryptedPassword

    fun matches(
        rawPassword: String,
        encrypted: EncryptedPassword,
    ): Boolean
}
