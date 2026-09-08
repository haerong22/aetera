package io.aetera.usecase.timeline

import io.aetera.model.module.TimelineEntry
import java.time.LocalDate

data class TimelineEntryDto(
    /** 어느 모듈이 낸 줄인지. 화면이 아이콘과 이름을 여기서 찾는다. */
    val moduleId: String,
    val on: LocalDate,
    val title: String,
    val detail: String?,
) {
    constructor(entry: TimelineEntry) : this(
        moduleId = entry.moduleId.value,
        on = entry.on,
        title = entry.title,
        detail = entry.detail,
    )
}
