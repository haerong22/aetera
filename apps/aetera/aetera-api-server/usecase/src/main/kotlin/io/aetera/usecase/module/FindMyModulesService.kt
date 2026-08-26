package io.aetera.usecase.module

import io.aetera.model.module.ModuleEnrollmentRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindMyModulesService(
    private val moduleRegistry: ModuleRegistry,
    private val moduleEnrollmentRepository: ModuleEnrollmentRepository,
) {
    /** 배포된 모든 모듈에 나의 사용 상태를 얹어서 돌려준다 — 모듈 스토어와 사이드바가 쓴다. */
    fun findMyModules(userId: UUID): List<ModuleSummaryDto> {
        val enrollments = moduleEnrollmentRepository.findAllByUserId(UserId(userId)).associateBy { it.moduleId }
        return moduleRegistry.descriptors.toSidebarOrder(enrollments)
    }
}
