package io.aetera.controller.schedule

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.schedule.CreateScheduleEventService
import io.aetera.usecase.schedule.DeleteScheduleEventService
import io.aetera.usecase.schedule.FindScheduleEventsService
import io.aetera.usecase.schedule.GetScheduleEventService
import io.aetera.usecase.schedule.ScheduleEventDto
import io.aetera.usecase.schedule.UpdateScheduleEventService
import io.aetera.usecase.schedule.cmd.FindScheduleEventsCommand
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.Instant
import java.util.UUID

/**
 * 일정 모듈의 API. `/api/v1/modules/schedule/..` 아래에 있으므로
 * 활성화 검사는 코어의 ModuleGuardInterceptor 가 대신한다 — 여기엔 그 코드가 없다.
 */
@RestController
@RequestMapping("/api/v1/modules/schedule/events")
@Tag(name = "Schedule")
class ScheduleEventController(
    private val createScheduleEventService: CreateScheduleEventService,
    private val getScheduleEventService: GetScheduleEventService,
    private val findScheduleEventsService: FindScheduleEventsService,
    private val updateScheduleEventService: UpdateScheduleEventService,
    private val deleteScheduleEventService: DeleteScheduleEventService,
) {
    @PostMapping
    @Operation(summary = "일정 생성")
    fun create(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody req: ScheduleEventReq,
    ): ResponseEntity<ScheduleEventDto> {
        val event = createScheduleEventService.create(req.toCreateCommand(userId))
        return ResponseEntity
            .created(URI.create("/api/v1/modules/schedule/events/${event.id}"))
            .body(event)
    }

    @GetMapping
    @Operation(summary = "기간 내 일정 조회. 기간과 겹치는 일정을 시작 시각 순으로 돌려준다.")
    fun findEvents(
        @CurrentUserId userId: UUID,
        @RequestParam("from")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        from: Instant,
        @RequestParam("to")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        to: Instant,
    ): List<ScheduleEventDto> = findScheduleEventsService.findEvents(
        FindScheduleEventsCommand(userId = userId, from = from, to = to),
    )

    @GetMapping("/{event-id}")
    @Operation(summary = "일정 단건 조회")
    fun getEvent(
        @CurrentUserId userId: UUID,
        @PathVariable("event-id") eventId: UUID,
    ): ScheduleEventDto = getScheduleEventService.getEvent(userId, eventId)

    @PutMapping("/{event-id}")
    @Operation(summary = "일정 수정")
    fun update(
        @CurrentUserId userId: UUID,
        @PathVariable("event-id") eventId: UUID,
        @Valid @RequestBody req: ScheduleEventReq,
    ): ScheduleEventDto = updateScheduleEventService.update(req.toUpdateCommand(userId, eventId))

    @DeleteMapping("/{event-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "일정 삭제")
    fun delete(
        @CurrentUserId userId: UUID,
        @PathVariable("event-id") eventId: UUID,
    ) {
        deleteScheduleEventService.delete(userId, eventId)
    }
}
