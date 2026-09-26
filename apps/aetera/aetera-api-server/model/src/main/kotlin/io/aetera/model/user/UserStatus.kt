package io.aetera.model.user

/**
 * 계정 상태.
 *
 * 탈퇴는 여기 값을 늘리지 않는다 — 행을 지우기 때문이다([UserRepository.delete]).
 * 그래서 지금은 하나뿐이고, 정지(suspend)처럼 "있지만 못 쓰는" 상태가 생기면 늘어난다.
 */
enum class UserStatus {
    ACTIVE,
}
