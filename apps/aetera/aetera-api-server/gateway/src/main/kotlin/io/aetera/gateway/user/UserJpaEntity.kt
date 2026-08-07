package io.aetera.gateway.user

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.user.Email
import io.aetera.model.user.User
import io.aetera.model.user.UserId
import io.aetera.model.user.UserStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@Entity
@Table(name = "users")
class UserJpaEntity(
    uid: UUID,
    @Column(name = "email", nullable = false, length = 320)
    var email: String,
    @Column(name = "nickname", nullable = false, length = 30)
    var nickname: String,
    @Column(name = "timezone", nullable = false, length = 50)
    var timezone: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: UserStatus,
    @Column(name = "registered_at", nullable = false, updatable = false)
    val registeredAt: Instant,
    @Column(name = "withdrawn_at")
    var withdrawnAt: Instant?,
) : UuidJpaEntity(uid) {
    fun applyFrom(user: User) {
        email = user.email.value
        nickname = user.nickname
        timezone = user.timezone.id
        status = user.status
        withdrawnAt = user.withdrawnAt
    }

    fun toModel(): User = User.reconstitute(
        id = UserId(uid),
        email = Email(email),
        nickname = nickname,
        timezone = ZoneId.of(timezone),
        status = status,
        registeredAt = registeredAt,
        withdrawnAt = withdrawnAt,
    )

    companion object {
        fun from(user: User): UserJpaEntity = UserJpaEntity(
            uid = user.id.value,
            email = user.email.value,
            nickname = user.nickname,
            timezone = user.timezone.id,
            status = user.status,
            registeredAt = user.registeredAt,
            withdrawnAt = user.withdrawnAt,
        )
    }
}
