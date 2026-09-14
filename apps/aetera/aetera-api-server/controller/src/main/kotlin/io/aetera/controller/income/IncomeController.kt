package io.aetera.controller.income

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.income.CreateIncomeService
import io.aetera.usecase.income.DeleteIncomeService
import io.aetera.usecase.income.FindIncomesService
import io.aetera.usecase.income.IncomeBoardDto
import io.aetera.usecase.income.UpdateIncomeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * 소득 모듈의 API. `/api/v1/modules/income/..` 아래에 있으므로
 * 활성화 검사는 코어의 ModuleGuardInterceptor 가 대신한다.
 *
 * 변경 API 가 전부 화면 전체([IncomeBoardDto])를 돌려준다 — 항목 하나가 바뀌면 합계가
 * 함께 움직이는데, 부분 응답을 주면 프론트가 그 합계를 다시 계산해야 한다.
 */
@RestController
@RequestMapping("/api/v1/modules/income/items")
@Tag(name = "Income")
class IncomeController(
    private val findIncomesService: FindIncomesService,
    private val createIncomeService: CreateIncomeService,
    private val updateIncomeService: UpdateIncomeService,
    private val deleteIncomeService: DeleteIncomeService,
) {
    @GetMapping
    @Operation(summary = "소득 목록과 합계. 큰 것부터 온다.")
    fun findIncomes(
        @CurrentUserId userId: UUID,
    ): IncomeBoardDto = findIncomesService.findIncomes(userId)

    @PostMapping
    @Operation(summary = "소득 등록")
    fun create(
        @CurrentUserId userId: UUID,
        @RequestBody req: IncomeReq,
    ): IncomeBoardDto = createIncomeService.create(req.toCommand(userId))

    @PutMapping("/{income-id}")
    @Operation(summary = "소득 수정")
    fun update(
        @CurrentUserId userId: UUID,
        @PathVariable("income-id") incomeId: UUID,
        @RequestBody req: IncomeReq,
    ): IncomeBoardDto = updateIncomeService.update(incomeId, req.toCommand(userId))

    @DeleteMapping("/{income-id}")
    @Operation(summary = "소득 삭제. 남은 목록과 합계를 돌려준다.")
    fun delete(
        @CurrentUserId userId: UUID,
        @PathVariable("income-id") incomeId: UUID,
    ): IncomeBoardDto = deleteIncomeService.delete(userId, incomeId)
}
