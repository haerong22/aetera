package io.aetera.gateway.module

import io.aetera.model.module.EnrollmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ModuleEnrollmentJpaRepository : JpaRepository<ModuleEnrollmentJpaEntity, UUID> {
    fun findByUserIdAndModuleId(
        userId: UUID,
        moduleId: String,
    ): ModuleEnrollmentJpaEntity?

    fun findAllByUserId(userId: UUID): List<ModuleEnrollmentJpaEntity>

    fun existsByUserIdAndModuleIdAndStatus(
        userId: UUID,
        moduleId: String,
        status: EnrollmentStatus,
    ): Boolean
}
