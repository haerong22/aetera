package io.aetera.model.notification

import io.aetera.model.common.UserOwned
import io.aetera.model.user.UserId
import io.aetera.shared.error.ensure
import java.time.LocalDate
import java.util.UUID

/**
 * 알림을 받을지, 몇 시에 받을지.
 *
 * 시각은 **사용자의 시간대 기준**이다. 잡은 한 시간마다 돌면서 "지금 그 사람에게 몇 시인가"를
 * 보고 고르므로, 서울에 있든 베를린에 있든 각자의 아침에 받는다.
 *
 * [lastSentOn] 이 이 클래스의 핵심이다. 잡이 두 번 돌거나 서버가 여러 대여도
 * **하루에 한 통을 넘지 않게** 막는 유일한 장치다 — 메일은 되돌릴 수 없어서
 * "아마 안 보냈겠지"로 넘길 수 없다.
 */
class NotificationPreference private constructor(
    val id: NotificationPreferenceId,
    override val userId: UserId,
    enabled: Boolean,
    sendHour: Int,
    lastSentOn: LocalDate?,
) : UserOwned {
    var enabled: Boolean = enabled
        private set

    /** 받을 시각(0~23). 사용자의 시간대에서의 시각이다. */
    var sendHour: Int = sendHour
        private set

    /** 마지막으로 보낸 날(사용자 기준). 아직 보낸 적이 없으면 null. */
    var lastSentOn: LocalDate? = lastSentOn
        private set

    fun change(
        enabled: Boolean,
        sendHour: Int,
    ) {
        this.enabled = enabled
        this.sendHour = validateSendHour(sendHour)
    }

    /**
     * 지금 보낼 때인가.
     *
     * 시각이 **지났으면** 보낸다(`>=`). 딱 그 시각에만 보내면 서버가 잠깐 멈췄다 살아난 날은
     * 그날 알림이 통째로 사라진다. 같은 날 두 번 가는 일은 [lastSentOn] 이 막는다.
     */
    fun shouldSend(
        localDate: LocalDate,
        localHour: Int,
    ): Boolean = enabled && localHour >= sendHour && lastSentOn != localDate

    fun markSent(localDate: LocalDate) {
        lastSentOn = localDate
    }

    override fun equals(other: Any?): Boolean = this === other || (other is NotificationPreference && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    companion object {
        /** 아침에 한 번. 출근 전에 보고 하루를 정할 수 있는 시각이다. */
        const val DEFAULT_SEND_HOUR: Int = 8

        fun create(
            id: NotificationPreferenceId,
            userId: UserId,
            enabled: Boolean = true,
            sendHour: Int = DEFAULT_SEND_HOUR,
        ): NotificationPreference = NotificationPreference(id, userId, enabled, validateSendHour(sendHour), null)

        fun reconstitute(
            id: NotificationPreferenceId,
            userId: UserId,
            enabled: Boolean,
            sendHour: Int,
            lastSentOn: LocalDate?,
        ): NotificationPreference = NotificationPreference(id, userId, enabled, sendHour, lastSentOn)

        private fun validateSendHour(sendHour: Int): Int {
            ensure(
                sendHour in 0..23,
                NotificationErrorCode.INVALID_SEND_HOUR,
                "받을 시각은 0시부터 23시 사이여야 합니다. 입력: $sendHour",
            )
            return sendHour
        }
    }
}

@JvmInline
value class NotificationPreferenceId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()

    companion object {
        fun next(): NotificationPreferenceId = NotificationPreferenceId(UUID.randomUUID())
    }
}
