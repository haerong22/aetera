package io.aetera.usecase.schedule

import io.aetera.model.module.ExportContributor
import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component

@Component
class ScheduleExportContributor(
    private val scheduleEventRepository: ScheduleEventRepository,
) : ExportContributor {
    override val section: String = ScheduleModule.MODULE_ID.value

    override fun exportFor(userId: UserId): List<Map<String, Any?>> = scheduleEventRepository
        .findAllByUserId(userId)
        .map { event ->
            mapOf(
                "title" to event.title,
                "description" to event.description,
                "startsAt" to event.startsAt.toString(),
                "endsAt" to event.endsAt.toString(),
                "allDay" to event.allDay,
                "color" to event.color,
                "createdAt" to event.createdAt.toString(),
            )
        }
}
