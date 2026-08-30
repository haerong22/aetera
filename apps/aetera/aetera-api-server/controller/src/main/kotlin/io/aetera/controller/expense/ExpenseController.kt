package io.aetera.controller.expense

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.expense.CreateExpenseService
import io.aetera.usecase.expense.DeleteExpenseService
import io.aetera.usecase.expense.ExpenseBoardDto
import io.aetera.usecase.expense.FindExpensesService
import io.aetera.usecase.expense.UpdateExpenseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
 * 고정지출 모듈의 API. `/api/v1/modules/expense/..` 아래에 있으므로
 * 활성화 검사는 코어의 ModuleGuardInterceptor 가 대신한다.
 *
 * 변경 API 가 전부 화면 전체([ExpenseBoardDto])를 돌려준다 — 항목 하나가 바뀌면 합계가
 * 함께 움직이는데, 부분 응답을 주면 프론트가 그 합계를 다시 계산해야 한다.
 */
@RestController
@RequestMapping("/api/v1/modules/expense/items")
@Tag(name = "Expense")
class ExpenseController(
    private val findExpensesService: FindExpensesService,
    private val createExpenseService: CreateExpenseService,
    private val updateExpenseService: UpdateExpenseService,
    private val deleteExpenseService: DeleteExpenseService,
) {
    @GetMapping
    @Operation(summary = "고정지출 목록과 합계. 부담이 큰 것부터 온다.")
    fun findExpenses(
        @CurrentUserId userId: UUID,
    ): ExpenseBoardDto = findExpensesService.findExpenses(userId)

    @PostMapping
    @Operation(summary = "고정지출 등록")
    fun create(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody req: ExpenseReq,
    ): ExpenseBoardDto = createExpenseService.create(req.toCommand(userId))

    @PutMapping("/{expense-id}")
    @Operation(summary = "고정지출 수정")
    fun update(
        @CurrentUserId userId: UUID,
        @PathVariable("expense-id") expenseId: UUID,
        @Valid @RequestBody req: ExpenseReq,
    ): ExpenseBoardDto = updateExpenseService.update(expenseId, req.toCommand(userId))

    @DeleteMapping("/{expense-id}")
    @Operation(summary = "고정지출 삭제. 남은 목록과 합계를 돌려준다.")
    fun delete(
        @CurrentUserId userId: UUID,
        @PathVariable("expense-id") expenseId: UUID,
    ): ExpenseBoardDto = deleteExpenseService.delete(userId, expenseId)
}
