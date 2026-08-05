package io.aetera.usecase.module

import io.aetera.model.module.ModuleEnrollmentRepository
import io.aetera.model.module.ModuleId
import io.aetera.model.user.UserId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

private val log = KotlinLogging.logger {}

@Service
class DisableModuleService(
    private val moduleRegistry: ModuleRegistry,
    private val moduleEnrollmentRepository: ModuleEnrollmentRepository,
    private val clock: Clock,
) {
    /** 소프트 비활성화 — 모듈이 쌓은 데이터는 남기고 접근만 막는다. */
    @Transactional
    fun disable(
        userId: UUID,
        moduleId: String,
    ): ModuleSummaryDto {
        val id = ModuleId(moduleId)
        val descriptor = moduleRegistry.getOrThrow(id)

        val enrollment =
            moduleEnrollmentRepository.getByUserIdAndModuleId(UserId(userId), id)?.let {
                it.disable(clock.instant())
                moduleEnrollmentRepository.save(it)
            }

        log.info { "모듈 비활성화 userId=$userId moduleId=$id" }
        return ModuleSummaryDto(descriptor, enrollment)
    }
}
