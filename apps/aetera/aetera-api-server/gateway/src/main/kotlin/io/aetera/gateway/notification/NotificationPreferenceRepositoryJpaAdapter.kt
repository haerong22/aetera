package io.aetera.gateway.notification

import io.aetera.gateway.common.saveMerging
import io.aetera.model.notification.NotificationPreference
import io.aetera.model.notification.NotificationPreferenceRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Repository

@Repository
class NotificationPreferenceRepositoryJpaAdapter(
    private val notificationPreferenceJpaRepository: NotificationPreferenceJpaRepository,
) : NotificationPreferenceRepository {
    override fun save(preference: NotificationPreference): NotificationPreference = notificationPreferenceJpaRepository
        .saveMerging(
            id = preference.id.value,
            update = { it.applyFrom(preference) },
            create = { NotificationPreferenceJpaEntity.from(preference) },
        ).toModel()

    override fun findByUserId(userId: UserId): NotificationPreference? = notificationPreferenceJpaRepository
        .findByUserId(userId.value)
        ?.toModel()

    override fun findAllEnabled(): List<NotificationPreference> = notificationPreferenceJpaRepository
        .findAllByEnabledIsTrue()
        .map { it.toModel() }
}
