package io.aetera.gateway.notification

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationPreferenceJpaRepository : JpaRepository<NotificationPreferenceJpaEntity, UUID> {
    fun findByUserId(userId: UUID): NotificationPreferenceJpaEntity?

    fun findAllByEnabledIsTrue(): List<NotificationPreferenceJpaEntity>
}
