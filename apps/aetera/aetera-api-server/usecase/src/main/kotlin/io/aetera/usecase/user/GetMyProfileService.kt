package io.aetera.usecase.user

import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetMyProfileService(
    private val userRepository: UserRepository,
) {
    fun getMyProfile(userId: UUID): UserDto = UserDto(userRepository.getByIdOrThrow(UserId(userId)))
}
