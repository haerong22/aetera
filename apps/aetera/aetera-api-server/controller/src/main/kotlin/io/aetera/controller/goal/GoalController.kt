package io.aetera.controller.goal

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.goal.AddGoalProgressService
import io.aetera.usecase.goal.CreateGoalService
import io.aetera.usecase.goal.DeleteGoalService
import io.aetera.usecase.goal.FindGoalsService
import io.aetera.usecase.goal.GoalDto
import io.aetera.usecase.goal.UpdateGoalService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

/**
 * 목표 모듈의 API. `/api/v1/modules/goal/..` 아래에 있으므로
 * 활성화 검사는 코어의 ModuleGuardInterceptor 가 대신한다.
 */
@RestController
@RequestMapping("/api/v1/modules/goal/goals")
@Tag(name = "Goal")
class GoalController(
    private val findGoalsService: FindGoalsService,
    private val createGoalService: CreateGoalService,
    private val updateGoalService: UpdateGoalService,
    private val addGoalProgressService: AddGoalProgressService,
    private val deleteGoalService: DeleteGoalService,
) {
    @GetMapping
    @Operation(summary = "목표 목록. 주기가 지난 목표는 0 부터 다시 보인다.")
    fun findGoals(
        @CurrentUserId userId: UUID,
    ): List<GoalDto> = findGoalsService.findGoals(userId)

    @PostMapping
    @Operation(summary = "목표 등록")
    fun create(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody req: GoalReq,
    ): ResponseEntity<GoalDto> {
        val created = createGoalService.create(req.toCommand(userId))
        return ResponseEntity.created(URI.create("/api/v1/modules/goal/goals/${created.id}")).body(created)
    }

    @PutMapping("/{goal-id}")
    @Operation(summary = "목표 수정. 주기를 바꾸면 진행도는 0 부터 다시 센다.")
    fun update(
        @CurrentUserId userId: UUID,
        @PathVariable("goal-id") goalId: UUID,
        @Valid @RequestBody req: GoalReq,
    ): GoalDto = updateGoalService.update(goalId, req.toCommand(userId))

    @PostMapping("/{goal-id}/progress")
    @Operation(summary = "진행 기록. 음수를 주면 되돌린다.")
    fun addProgress(
        @CurrentUserId userId: UUID,
        @PathVariable("goal-id") goalId: UUID,
        @Valid @RequestBody(required = false) req: AddProgressReq?,
    ): GoalDto = addGoalProgressService.addProgress(userId, goalId, req?.amount ?: 1)

    @DeleteMapping("/{goal-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "목표 삭제")
    fun delete(
        @CurrentUserId userId: UUID,
        @PathVariable("goal-id") goalId: UUID,
    ) {
        deleteGoalService.delete(userId, goalId)
    }
}
