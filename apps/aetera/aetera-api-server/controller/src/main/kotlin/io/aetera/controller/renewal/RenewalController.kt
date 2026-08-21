package io.aetera.controller.renewal

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.renewal.CreateRenewalService
import io.aetera.usecase.renewal.DeleteRenewalService
import io.aetera.usecase.renewal.FindRenewalsService
import io.aetera.usecase.renewal.RenewRenewalService
import io.aetera.usecase.renewal.RenewalDto
import io.aetera.usecase.renewal.UpdateRenewalService
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
 * 만기 관리 모듈의 API. `/api/v1/modules/renewal/..` 아래에 있으므로
 * 활성화 검사는 코어의 ModuleGuardInterceptor 가 대신한다.
 */
@RestController
@RequestMapping("/api/v1/modules/renewal/items")
@Tag(name = "Renewal")
class RenewalController(
    private val findRenewalsService: FindRenewalsService,
    private val createRenewalService: CreateRenewalService,
    private val updateRenewalService: UpdateRenewalService,
    private val renewRenewalService: RenewRenewalService,
    private val deleteRenewalService: DeleteRenewalService,
) {
    @GetMapping
    @Operation(summary = "만기 항목 목록. 만기가 이른 것부터 온다.")
    fun findRenewals(
        @CurrentUserId userId: UUID,
    ): List<RenewalDto> = findRenewalsService.findRenewals(userId)

    @PostMapping
    @Operation(summary = "만기 항목 등록")
    fun create(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody req: RenewalReq,
    ): ResponseEntity<RenewalDto> {
        val created = createRenewalService.create(req.toCommand(userId))
        return ResponseEntity.created(URI.create("/api/v1/modules/renewal/items/${created.id}")).body(created)
    }

    @PutMapping("/{renewal-id}")
    @Operation(summary = "만기 항목 수정")
    fun update(
        @CurrentUserId userId: UUID,
        @PathVariable("renewal-id") renewalId: UUID,
        @Valid @RequestBody req: RenewalReq,
    ): RenewalDto = updateRenewalService.update(renewalId, req.toCommand(userId))

    @PostMapping("/{renewal-id}/renewals")
    @Operation(summary = "갱신 완료 표시. 날짜를 주면 그 날로, 없으면 주기만큼 넘어간다.")
    fun renew(
        @CurrentUserId userId: UUID,
        @PathVariable("renewal-id") renewalId: UUID,
        @RequestBody(required = false) req: RenewReq?,
    ): RenewalDto = renewRenewalService.renew(userId, renewalId, req?.nextExpiresAt)

    @DeleteMapping("/{renewal-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "만기 항목 삭제")
    fun delete(
        @CurrentUserId userId: UUID,
        @PathVariable("renewal-id") renewalId: UUID,
    ) {
        deleteRenewalService.delete(userId, renewalId)
    }
}
