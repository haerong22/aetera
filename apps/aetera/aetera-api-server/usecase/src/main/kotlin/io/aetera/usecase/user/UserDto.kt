package io.aetera.usecase.user

import io.aetera.model.user.User
import java.time.Instant
import java.util.UUID

data class UserDto(
    val id: UUID,
    val email: String,
    val nickname: String,
    val timezone: String,
    val registeredAt: Instant,
) {
    constructor(user: User) : this(
        id = user.id.value,
        email = user.email.value,
        nickname = user.nickname,
        timezone = user.timezone.id,
        registeredAt = user.registeredAt,
    )
}
