package io.aetera.gateway.schedule

import io.aetera.model.schedule.ScheduleEvent
import io.aetera.model.schedule.ScheduleEventId
import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.schedule.SchedulePeriod
import io.aetera.model.user.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ScheduleEventRepositoryJpaAdapter(
    private val scheduleEventJpaRepository: ScheduleEventJpaRepository,
) : ScheduleEventRepository {
    override fun save(event: ScheduleEvent): ScheduleEvent {
        val entity =
            scheduleEventJpaRepository.findByIdOrNull(event.id.value)?.apply { applyFrom(event) }
                ?: ScheduleEventJpaEntity.from(event)
        return scheduleEventJpaRepository.save(entity).toModel()
    }

    override fun getById(id: ScheduleEventId): ScheduleEvent? = scheduleEventJpaRepository.findByIdOrNull(id.value)?.toModel()

    override fun findAllOverlapping(
        userId: UserId,
        period: SchedulePeriod,
    ): List<ScheduleEvent> = scheduleEventJpaRepository
        .findAllOverlapping(
            userId = userId.value,
            from = period.from,
            to = period.to,
        ).map { it.toModel() }

    override fun delete(event: ScheduleEvent) {
        scheduleEventJpaRepository.deleteById(event.id.value)
    }
}
