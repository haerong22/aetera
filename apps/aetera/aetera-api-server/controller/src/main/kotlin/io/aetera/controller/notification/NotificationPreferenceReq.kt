package io.aetera.controller.notification

import io.swagger.v3.oas.annotations.media.Schema

data class NotificationPreferenceReq(
    val enabled: Boolean,
    /** 받을 시각(0~23). 사용자의 시간대 기준. 범위는 모델이 본다. */
    @field:Schema(example = "8")
    val sendHour: Int,
)
