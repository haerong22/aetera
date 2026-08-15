package io.aetera.gateway.guide

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.guide.GuideJourneyId
import io.aetera.model.guide.GuideTaskKey
import io.aetera.model.guide.GuideTaskProgress
import io.aetera.model.guide.GuideTaskProgressId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "guide_task_progresses")
class GuideTaskProgressJpaEntity(
    uid: UUID,
    @Column(name = "journey_id", nullable = false, updatable = false)
    val journeyId: UUID,
    @Column(name = "task_key", nullable = false, length = 80, updatable = false)
    val taskKey: String,
    @Column(name = "done", nullable = false)
    var done: Boolean,
    @Column(name = "note", columnDefinition = "text")
    var note: String?,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
) : UuidJpaEntity(uid) {
    fun applyFrom(progress: GuideTaskProgress) {
        done = progress.done
        note = progress.note
        updatedAt = progress.updatedAt
    }

    fun toModel(): GuideTaskProgress = GuideTaskProgress.reconstitute(
        id = GuideTaskProgressId(uid),
        journeyId = GuideJourneyId(journeyId),
        taskKey = GuideTaskKey(taskKey),
        done = done,
        note = note,
        updatedAt = updatedAt,
    )

    companion object {
        fun from(progress: GuideTaskProgress): GuideTaskProgressJpaEntity = GuideTaskProgressJpaEntity(
            uid = progress.id.value,
            journeyId = progress.journeyId.value,
            taskKey = progress.taskKey.value,
            done = progress.done,
            note = progress.note,
            updatedAt = progress.updatedAt,
        )
    }
}
