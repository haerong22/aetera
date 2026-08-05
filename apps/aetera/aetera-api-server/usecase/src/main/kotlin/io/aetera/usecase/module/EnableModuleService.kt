package io.aetera.usecase.module

import io.aetera.model.module.ModuleEnrollment
import io.aetera.model.module.ModuleEnrollmentId
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
class EnableModuleService(
    private val moduleRegistry: ModuleRegistry,
    private val moduleEnrollmentRepository: ModuleEnrollmentRepository,
    private val clock: Clock,
) {
    /** 몇 번을 눌러도 같은 결과다. 비활성 이력이 있으면 데이터를 그대로 되살린다. */
    @Transactional
    fun enable(
        userId: UUID,
        moduleId: String,
    ): ModuleSummaryDto {
        val id = ModuleId(moduleId)
        val descriptor = moduleRegistry.getOrThrow(id)
        val owner = UserId(userId)

        val enrollment =
            moduleEnrollmentRepository.getByUserIdAndModuleId(owner, id)?.apply { enable(clock.instant()) }
                ?: ModuleEnrollment.enable(
                    id = ModuleEnrollmentId.next(),
                    userId = owner,
                    moduleId = id,
                    at = clock.instant(),
                )

        log.info { "모듈 활성화 userId=$owner moduleId=$id" }
        return ModuleSummaryDto(descriptor, moduleEnrollmentRepository.save(enrollment))
    }
}
