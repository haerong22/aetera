package io.aetera.gateway.user

import io.aetera.gateway.common.saveMerging
import io.aetera.model.user.Email
import io.aetera.model.user.User
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryJpaAdapter(
    private val userJpaRepository: UserJpaRepository,
) : UserRepository {
    override fun save(user: User): User = userJpaRepository
        .saveMerging(
            id = user.id.value,
            update = { it.applyFrom(user) },
            create = { UserJpaEntity.from(user) },
        ).toModel()

    override fun getById(id: UserId): User? = userJpaRepository.findByIdOrNull(id.value)?.toModel()

    override fun getByEmail(email: Email): User? = userJpaRepository.findByEmail(email.value)?.toModel()

    override fun existsByEmail(email: Email): Boolean = userJpaRepository.existsByEmail(email.value)
}
