package io.aetera.usecase.module

import io.aetera.model.module.ModuleCategory
import io.aetera.model.module.ModuleDescriptor
import io.aetera.model.module.ModuleEnrollment
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
