package io.aetera.usecase.module

import io.aetera.model.module.ModuleCategory
import io.aetera.model.module.ModuleDescriptor
import io.aetera.model.module.ModuleEnrollment
import io.aetera.model.module.ModuleId
import java.time.Instant

/** 모듈 스토어 화면 한 줄: 모듈 소개 ⊕ 나의 사용 상태. */
data class ModuleSummaryDto(
    val id: String,
    val displayName: String,
    val description: String,
    val category: ModuleCategory,
    val version: String,
    val enabled: Boolean,
    val enabledAt: Instant?,
) {
    constructor(descriptor: ModuleDescriptor, enrollment: ModuleEnrollment?) : this(
        id = descriptor.id.value,
        displayName = descriptor.displayName,
        description = descriptor.description,
        category = descriptor.category,
        version = descriptor.version,
        enabled = enrollment?.isEnabled ?: false,
        enabledAt = enrollment?.takeIf { it.isEnabled }?.enabledAt,
    )
}

/**
 * 배포된 모듈에 내 사용 상태를 얹어 사이드바 순서대로 돌려준다.
 *
 * 순서가 같으면 아이디로 고정한다 — tiebreaker 가 없으면 같은 요청이 매번 다른 순서를 준다.
 * 응답에 순서 값을 싣지는 않는다. 배열 순서가 곧 순서이고 화면은 그걸 그대로 그린다.
 */
internal fun List<ModuleDescriptor>.toSidebarOrder(enrollments: Map<ModuleId, ModuleEnrollment>): List<ModuleSummaryDto> = this
    .sortedWith(
        compareBy(
            { enrollments[it.id]?.sortOrder ?: ModuleEnrollment.DEFAULT_SORT_ORDER },
            { it.id.value },
        ),
    ).map { ModuleSummaryDto(it, enrollments[it.id]) }
