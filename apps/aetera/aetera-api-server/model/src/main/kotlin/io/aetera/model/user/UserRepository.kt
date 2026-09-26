package io.aetera.model.user

interface UserRepository {
    fun save(user: User): User

    /**
     * 사용자를 **되돌릴 수 없이** 지운다.
     *
     * 상태만 바꿔 두면 이메일 유니크 인덱스가 그 주소를 영영 묶어,
     * 탈퇴한 사람이 같은 메일로 다시 올 수 없다.
     */
    fun delete(user: User)

    fun getById(id: UserId): User?

    fun getByEmail(email: Email): User?

    fun existsByEmail(email: Email): Boolean
}
