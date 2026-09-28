package io.aetera.usecase.user

import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 닉네임 변경.
 *
 * 이메일은 바꾸지 않는다 — 로그인 아이디이자 알림이 가는 곳이라, 바꾸려면 새 주소가
 * 진짜 그 사람 것인지 확인하는 절차가 먼저 있어야 한다. 그 절차가 생기기 전까지는
 * 바꿀 수 있는 척하지 않는 편이 낫다.
 */
@Service
class ChangeNicknameService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun changeNickname(
        userId: UUID,
        nickname: String,
    ): UserDto {
        val user = userRepository.getByIdOrThrow(UserId(userId))
        // 다듬기와 길이 제한은 User 가 안다. 여기서 또 보면 두 곳이 서로 달라질 자리가 생긴다.
        user.changeNickname(nickname)
        return UserDto(userRepository.save(user))
    }
}
