package io.aetera.controller.spending

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.spending.DeleteSpendingService
import io.aetera.usecase.spending.FindSpendingService
import io.aetera.usecase.spending.SaveSpendingService
import io.aetera.usecase.spending.SpendingBoardDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

/**
 * 변동지출 모듈의 API. `/api/v1/modules/spending/..` 아래라 활성화 검사는 코어 가드가 한다.
 *
 * 쓰기는 한 달을 통째로 PUT 하는 것 하나뿐이다. 한 달에 적을 것이 숫자 하나라
 * 부분 수정이라는 것이 없다 — 같은 달로 몇 번을 보내도 결과가 같다.
 *
 * `today` 를 쿼리로 받는다. 평균이 "최근 몇 달"을 보려면 오늘이 언제인지 알아야 하는데
 * **서버의 오늘은 사용자의 오늘이 아니다** — 시간대가 다르면 달이 하나 어긋난다.
 * 타임라인이 `from`/`to` 를 받는 것과 같은 이유다.
 */
@RestController
@RequestMapping("/api/v1/modules/spending/records")
@Tag(name = "Spending")
class SpendingController(
    private val findSpendingService: FindSpendingService,
    private val saveSpendingService: SaveSpendingService,
    private val deleteSpendingService: DeleteSpendingService,
) {
    @GetMapping
    @Operation(summary = "달별 기록과 최근 평균")
    fun findSpending(
        @CurrentUserId userId: UUID,
        @RequestParam("today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) today: LocalDate,
    ): SpendingBoardDto = findSpendingService.findBoard(userId, today)

    @PutMapping("/{month}")
    @Operation(summary = "한 달치 기록. 같은 달로 몇 번을 보내도 결과가 같다.")
    fun save(
        @CurrentUserId userId: UUID,
        @PathVariable("month") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) month: LocalDate,
        @RequestParam("today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) today: LocalDate,
        @RequestBody req: SpendingReq,
    ): SpendingBoardDto {
        saveSpendingService.save(req.toCommand(userId, month), today)
        return findSpendingService.findBoard(userId, today)
    }

    /** 없는 달을 지우면 404 다 — 조용히 성공으로 답하면 다른 사람의 달을 지우려 한 것도 성공이 된다. */
    @DeleteMapping("/{month}")
    @Operation(summary = "한 달치 기록 삭제")
    fun delete(
        @CurrentUserId userId: UUID,
        @PathVariable("month") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) month: LocalDate,
        @RequestParam("today") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) today: LocalDate,
    ): SpendingBoardDto {
        deleteSpendingService.delete(userId, month)
        return findSpendingService.findBoard(userId, today)
    }
}
