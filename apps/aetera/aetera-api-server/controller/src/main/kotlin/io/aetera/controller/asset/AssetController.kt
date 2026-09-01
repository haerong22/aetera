package io.aetera.controller.asset

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.asset.AssetBoardDto
import io.aetera.usecase.asset.DeleteSnapshotService
import io.aetera.usecase.asset.FindAssetsService
import io.aetera.usecase.asset.SaveSnapshotService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

/**
 * 자산 모듈의 API. `/api/v1/modules/asset/..` 아래에 있으므로
 * 활성화 검사는 코어의 ModuleGuardInterceptor 가 대신한다.
 *
 * 줄 단위 API 가 없다. 한 달치를 통째로 PUT 하는 것이 이 모듈의 유일한 쓰기다 —
 * 스냅샷은 "그 달의 상태 전체"라서 부분 수정이 반쪽짜리 사실을 만든다.
 */
@RestController
@RequestMapping("/api/v1/modules/asset/snapshots")
@Tag(name = "Asset")
class AssetController(
    private val findAssetsService: FindAssetsService,
    private val saveSnapshotService: SaveSnapshotService,
    private val deleteSnapshotService: DeleteSnapshotService,
) {
    @GetMapping
    @Operation(summary = "가장 최근 달의 자산과 순자산 추이")
    fun findAssets(
        @CurrentUserId userId: UUID,
    ): AssetBoardDto = findAssetsService.findAssets(userId)

    @PutMapping("/{month}")
    @Operation(summary = "한 달치 기록. 같은 달로 몇 번을 보내도 결과가 같다.")
    fun save(
        @CurrentUserId userId: UUID,
        @PathVariable("month") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) month: LocalDate,
        @RequestBody req: SnapshotReq,
    ): AssetBoardDto = saveSnapshotService.save(req.toCommand(userId, month))

    @DeleteMapping("/{month}")
    @Operation(summary = "한 달치 기록 삭제. 없는 달을 지워도 조용히 성공한다.")
    fun delete(
        @CurrentUserId userId: UUID,
        @PathVariable("month") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) month: LocalDate,
    ): AssetBoardDto = deleteSnapshotService.delete(userId, month)
}
