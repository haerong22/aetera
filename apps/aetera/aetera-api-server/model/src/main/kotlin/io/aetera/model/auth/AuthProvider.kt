package io.aetera.model.auth

/**
 * 인증 수단의 출처. 소셜 로그인을 추가할 때는 여기 항목을 늘리고
 * [AuthCredential] 행을 하나 더 저장하면 된다 — `User` 는 바뀌지 않는다.
 */
enum class AuthProvider {
    EMAIL,
    KAKAO,
}
