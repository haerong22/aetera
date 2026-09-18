package io.aetera.gateway.notification

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.notification.NotificationPreference
import io.aetera.model.notification.NotificationPreferenceId
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "notification_preferences")
class NotificationPreferenceJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "enabled", nullable = false)
    var enabled: Boolean,
    @Column(name = "send_hour", nullable = false)
    var sendHour: Int,
    @Column(name = "last_sent_on")
    var lastSentOn: LocalDate?,
) : UuidJpaEntity(uid) {
    fun applyFrom(preference: NotificationPreference) {
        enabled = preference.enabled
        sendHour = preference.sendHour
        lastSentOn = preference.lastSentOn
    }

    fun toModel(): NotificationPreference = NotificationPreference.reconstitute(
        id = NotificationPreferenceId(uid),
        userId = UserId(userId),
        enabled = enabled,
        sendHour = sendHour,
        lastSentOn = lastSentOn,
    )

    companion object {
        fun from(preference: NotificationPreference): NotificationPreferenceJpaEntity = NotificationPreferenceJpaEntity(
            uid = preference.id.value,
            userId = preference.userId.value,
            enabled = preference.enabled,
            sendHour = preference.sendHour,
            lastSentOn = preference.lastSentOn,
        )
    }
}
