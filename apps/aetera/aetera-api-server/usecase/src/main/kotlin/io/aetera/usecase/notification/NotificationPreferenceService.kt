package io.aetera.usecase.notification

import io.aetera.model.notification.NotificationPreference
import io.aetera.model.notification.NotificationPreferenceId
import io.aetera.model.notification.NotificationPreferenceRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 알림 설정을 읽고 바꾼다.
 *
 * 가입할 때 행을 만들지 않는다. **설정 화면을 한 번도 안 연 사람에게도 기본값이 있어야** 하는데,
 * 가입 시점에 만들면 그 전에 가입한 사람들은 행이 없어 잡이 건너뛴다. 대신 여기서
 * "없으면 기본값"으로 답하고, 실제 행은 처음 바꿀 때 생긴다.
 *
 * 그래서 [findOrDefault] 가 저장을 하지 않는다 — 조회가 쓰기를 일으키면 읽기 트랜잭션에서
 * 부를 수 없고, 목록을 그리는 것만으로 행이 우수수 생긴다.
 */
@Service
@Transactional(readOnly = true)
class NotificationPreferenceService(
    private val notificationPreferenceRepository: NotificationPreferenceRepository,
) {
    fun findOrDefault(userId: UUID): NotificationPreferenceDto {
        val owner = UserId(userId)
        val preference =
            notificationPreferenceRepository.findByUserId(owner)
                ?: NotificationPreference.create(NotificationPreferenceId.next(), owner)
        return NotificationPreferenceDto(preference)
    }

    @Transactional
    fun change(
        userId: UUID,
        enabled: Boolean,
        sendHour: Int,
    ): NotificationPreferenceDto {
        val owner = UserId(userId)
        val preference =
            notificationPreferenceRepository.findByUserId(owner)
                ?: NotificationPreference.create(NotificationPreferenceId.next(), owner)

        preference.change(enabled, sendHour)
        return NotificationPreferenceDto(notificationPreferenceRepository.save(preference))
    }
}
