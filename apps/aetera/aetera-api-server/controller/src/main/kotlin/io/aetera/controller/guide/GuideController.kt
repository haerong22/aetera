package io.aetera.controller.guide

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.guide.FindGuideService
import io.aetera.usecase.guide.GuideViewDto
import io.aetera.usecase.guide.ResetGuideJourneyService
import io.aetera.usecase.guide.StartGuideJourneyService
import io.aetera.usecase.guide.UpdateGuideTaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * 가이드형 모듈 전체의 API.
 *
 * 경로의 `{guide-id}` 가 곧 모듈 아이디라서 **가이드가 몇 개가 되든 컨트롤러는 이 하나다** —
 * 퇴사 준비, 결혼 준비, 이사가 전부 이 경로를 탄다. 사용자가 켜지 않은 가이드는
 * 코어의 ModuleGuardInterceptor 가 403 으로 자르므로 여기엔 활성화 검사 코드가 없다.
 *
 * 변경 API 가 전부 화면 전체(GuideViewDto)를 돌려주는 이유: 체크 하나에 진행률·완료 개수·
 * 단계별 상태가 함께 움직인다. 부분 응답을 주면 프론트가 그 파생값을 다시 계산해야 하고,
 * 그 계산이 서버와 어긋나는 순간을 사용자가 본다.
 */
@RestController
@RequestMapping("/api/v1/modules/{guide-id}/guide")
@Tag(name = "Guide")
class GuideController(
    private val findGuideService: FindGuideService,
    private val startGuideJourneyService: StartGuideJourneyService,
    private val resetGuideJourneyService: ResetGuideJourneyService,
    private val updateGuideTaskService: UpdateGuideTaskService,
) {
    @GetMapping
    @Operation(summary = "가이드 조회. 콘텐츠와 내 진행 상태를 한 번에 준다.")
    fun findGuide(
        @CurrentUserId userId: UUID,
        @PathVariable("guide-id") guideId: String,
    ): GuideViewDto = findGuideService.findGuide(userId, guideId)

    @PutMapping("/journey")
    @Operation(summary = "여정 시작 또는 기준일 변경. 몇 번을 호출해도 같은 결과이고, 체크해 둔 항목은 유지된다.")
    fun startJourney(
        @CurrentUserId userId: UUID,
        @PathVariable("guide-id") guideId: String,
        @Valid @RequestBody req: StartGuideJourneyReq,
    ): GuideViewDto = startGuideJourneyService.start(req.toCommand(userId, guideId))

    @DeleteMapping("/journey")
    @Operation(summary = "여정 초기화. 기준일과 체크 상태를 모두 지운다.")
    fun resetJourney(
        @CurrentUserId userId: UUID,
        @PathVariable("guide-id") guideId: String,
    ): GuideViewDto = resetGuideJourneyService.reset(userId, guideId)

    @PutMapping("/tasks/{task-key}")
    @Operation(summary = "할 일의 체크와 메모 저장")
    fun updateTask(
        @CurrentUserId userId: UUID,
        @PathVariable("guide-id") guideId: String,
        @PathVariable("task-key") taskKey: String,
        @Valid @RequestBody req: UpdateGuideTaskReq,
    ): GuideViewDto = updateGuideTaskService.updateTask(req.toCommand(userId, guideId, taskKey))
}
