package io.aetera.model.notification

import io.aetera.model.user.UserId

interface NotificationPreferenceRepository {
    fun save(preference: NotificationPreference): NotificationPreference

    fun findByUserId(userId: UserId): NotificationPreference?

    /**
     * 알림을 켠 사람들. 잡이 한 시간마다 부른다.
     *
     * 끈 사람은 아예 읽어 오지 않는다 — 보낼 일이 없는 사람의 모듈 데이터까지
     * 훑을 이유가 없고, 그 비용이 사용자 수만큼 는다.
     */
    fun findAllEnabled(): List<NotificationPreference>
}
