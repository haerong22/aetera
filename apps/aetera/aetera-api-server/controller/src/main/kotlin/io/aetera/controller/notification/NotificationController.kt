package io.aetera.controller.notification

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.notification.NotificationPreferenceDto
import io.aetera.usecase.notification.NotificationPreferenceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * 알림 설정. 모듈이 아니라 코어 기능이라 `/api/v1/me` 아래에 둔다 —
 * `/modules/..` 에 두면 코어의 활성화 가드가 "notification 모듈을 켜라"고 막는다.
 */
@RestController
@RequestMapping("/api/v1/me/notifications")
@Tag(name = "Notification")
class NotificationController(
    private val notificationPreferenceService: NotificationPreferenceService,
) {
    @GetMapping
    @Operation(summary = "알림 설정. 한 번도 바꾼 적이 없으면 기본값이 온다.")
    fun find(
        @CurrentUserId userId: UUID,
    ): NotificationPreferenceDto = notificationPreferenceService.findOrDefault(userId)

    @PutMapping
    @Operation(summary = "알림 설정 변경")
    fun change(
        @CurrentUserId userId: UUID,
        @RequestBody req: NotificationPreferenceReq,
    ): NotificationPreferenceDto = notificationPreferenceService.change(userId, req.enabled, req.sendHour)
}
