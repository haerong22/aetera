package io.aetera.infrastructure.security

import io.aetera.model.auth.EncryptedPassword
import io.aetera.model.auth.PasswordEncryptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@Component
class Pbkdf2PasswordEncryptor(
    @Value("\${security.password.iterations:210000}") private val iterations: Int,
) : PasswordEncryptor {
    private val secureRandom = SecureRandom()

    override fun encrypt(rawPassword: String): EncryptedPassword {
        val salt = ByteArray(SALT_BYTES)
        secureRandom.nextBytes(salt)
        val hash = hash(rawPassword, salt, iterations)
        return EncryptedPassword(
            listOf(ALGORITHM_TAG, iterations.toString(), encode(salt), encode(hash)).joinToString(SEPARATOR),
        )
    }

    override fun matches(
        rawPassword: String,
        encrypted: EncryptedPassword,
    ): Boolean {
        val parts = encrypted.value.split(SEPARATOR)
        if (parts.size != PART_COUNT || parts[0] != ALGORITHM_TAG) return false
        val storedIterations = parts[1].toIntOrNull() ?: return false
        val salt = decode(parts[2])
        val expected = decode(parts[3])
        return MessageDigest.isEqual(expected, hash(rawPassword, salt, storedIterations))
    }

    private fun hash(
        rawPassword: String,
        salt: ByteArray,
        iterationCount: Int,
    ): ByteArray {
        val spec = PBEKeySpec(rawPassword.toCharArray(), salt, iterationCount, KEY_BITS)
        return SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM).generateSecret(spec).encoded
    }

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    private fun decode(value: String): ByteArray = Base64.getDecoder().decode(value)

    companion object {
        private const val ALGORITHM_TAG = "pbkdf2-sha256"
        private const val SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val SEPARATOR = ":"
        private const val PART_COUNT = 4
        private const val SALT_BYTES = 16
        private const val KEY_BITS = 256
    }
}
