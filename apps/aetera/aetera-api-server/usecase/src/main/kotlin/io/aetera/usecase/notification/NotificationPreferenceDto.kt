package io.aetera.usecase.notification

import io.aetera.model.notification.NotificationPreference

data class NotificationPreferenceDto(
    val enabled: Boolean,
    /** 받을 시각(0~23). 사용자의 시간대 기준이다. */
    val sendHour: Int,
) {
    constructor(preference: NotificationPreference) : this(
        enabled = preference.enabled,
        sendHour = preference.sendHour,
    )
}
