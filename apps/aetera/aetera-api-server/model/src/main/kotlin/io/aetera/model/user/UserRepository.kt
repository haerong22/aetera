package io.aetera.model.user

interface UserRepository {
    fun save(user: User): User

    fun getById(id: UserId): User?

    fun getByEmail(email: Email): User?

    fun existsByEmail(email: Email): Boolean
}
