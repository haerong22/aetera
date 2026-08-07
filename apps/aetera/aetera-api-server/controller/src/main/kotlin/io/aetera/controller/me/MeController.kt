package io.aetera.controller.me

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.module.DisableModuleService
import io.aetera.usecase.module.EnableModuleService
import io.aetera.usecase.module.FindMyModulesService
import io.aetera.usecase.module.ModuleSummaryDto
import io.aetera.usecase.user.GetMyProfileService
import io.aetera.usecase.user.UserDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Me")
class MeController(
    private val getMyProfileService: GetMyProfileService,
    private val findMyModulesService: FindMyModulesService,
    private val enableModuleService: EnableModuleService,
    private val disableModuleService: DisableModuleService,
) {
    @GetMapping
    @Operation(summary = "내 프로필 조회")
    fun getMe(
        @CurrentUserId userId: UUID,
    ): UserDto = getMyProfileService.getMyProfile(userId)

    @GetMapping("/modules")
    @Operation(summary = "모듈 목록 조회. 배포된 모든 모듈에 나의 사용 상태가 얹혀 온다.")
    fun findMyModules(
        @CurrentUserId userId: UUID,
    ): List<ModuleSummaryDto> = findMyModulesService.findMyModules(userId)

    @PostMapping("/modules/{module-id}/enablement")
    @Operation(summary = "모듈 사용 시작. 몇 번을 호출해도 같은 결과다.")
    fun enableModule(
        @CurrentUserId userId: UUID,
        @PathVariable("module-id") moduleId: String,
    ): ModuleSummaryDto = enableModuleService.enable(userId, moduleId)

    @DeleteMapping("/modules/{module-id}/enablement")
    @Operation(summary = "모듈 사용 중지. 데이터는 남고 접근만 막힌다.")
    fun disableModule(
        @CurrentUserId userId: UUID,
        @PathVariable("module-id") moduleId: String,
    ): ModuleSummaryDto = disableModuleService.disable(userId, moduleId)
}
