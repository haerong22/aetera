package io.aetera.controller.export

import io.aetera.controller.common.CurrentUserId
import io.aetera.usecase.export.ExportMyDataService
import io.aetera.usecase.export.MyDataDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * 내 데이터 내보내기. 모듈이 아니라 코어 기능이라 `/api/v1/me` 아래에 둔다 —
 * `/modules/..` 에 두면 코어의 활성화 가드가 막는다.
 */
@RestController
@RequestMapping("/api/v1/me/export")
@Tag(name = "Export")
class ExportController(
    private val exportMyDataService: ExportMyDataService,
    private val clock: Clock,
) {
    private companion object {
        val FILE_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    /**
     * 브라우저가 **바로 파일로 저장하게** 한다(`Content-Disposition: attachment`).
     * 이게 없으면 JSON 이 탭에 펼쳐지고, 사용자는 그걸 긁어서 저장해야 한다.
     */
    @GetMapping
    @Operation(summary = "내 데이터 전부를 JSON 파일로 받는다. 끈 모듈의 데이터도 함께 온다.")
    fun export(
        @CurrentUserId userId: UUID,
    ): ResponseEntity<MyDataDto> {
        val today = clock.instant().atZone(ZoneOffset.UTC).format(FILE_DATE)
        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aetera-$today.json\"")
            .contentType(MediaType.APPLICATION_JSON)
            .body(exportMyDataService.export(userId))
    }
}
