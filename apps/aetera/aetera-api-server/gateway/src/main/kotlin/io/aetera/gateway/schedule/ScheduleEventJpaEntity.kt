package io.aetera.gateway.schedule

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.schedule.ScheduleEvent
import io.aetera.model.schedule.ScheduleEventId
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "schedule_events")
class ScheduleEventJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "title", nullable = false, length = 200)
    var title: String,
    @Column(name = "description", columnDefinition = "text")
    var description: String?,
    @Column(name = "starts_at", nullable = false)
    var startsAt: Instant,
    @Column(name = "ends_at", nullable = false)
    var endsAt: Instant,
    @Column(name = "all_day", nullable = false)
    var allDay: Boolean,
    @Column(name = "color", length = 7)
    var color: String?,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
) : UuidJpaEntity(uid) {
    fun applyFrom(event: ScheduleEvent) {
        title = event.title
        description = event.description
        startsAt = event.startsAt
        endsAt = event.endsAt
        allDay = event.allDay
        color = event.color
    }

    fun toModel(): ScheduleEvent = ScheduleEvent.reconstitute(
        id = ScheduleEventId(uid),
        userId = UserId(userId),
        title = title,
        description = description,
        startsAt = startsAt,
        endsAt = endsAt,
        allDay = allDay,
        color = color,
        createdAt = createdAt,
    )

    companion object {
        fun from(event: ScheduleEvent): ScheduleEventJpaEntity = ScheduleEventJpaEntity(
            uid = event.id.value,
            userId = event.userId.value,
            title = event.title,
            description = event.description,
            startsAt = event.startsAt,
            endsAt = event.endsAt,
            allDay = event.allDay,
            color = event.color,
            createdAt = event.createdAt,
        )
    }
}
