package io.aetera.model.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * 리프레시 토큰 원문 생성과 해시. JDK 만 쓰므로 도메인에 둔다.
 *
 * 원문은 256bit 난수의 base64url 표현이라 추측이 불가능하고, 저장은 SHA-256 해시로만 한다 —
 * DB 가 유출돼도 세션을 탈취할 수 없다.
 */
object OpaqueToken {
    private const val TOKEN_BYTES = 32

    private val secureRandom = SecureRandom()

    fun generate(): String {
        val bytes = ByteArray(TOKEN_BYTES)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun hash(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}
