package io.aetera.gateway.module

import io.aetera.gateway.common.saveMerging
import io.aetera.model.module.EnrollmentStatus
import io.aetera.model.module.ModuleEnrollment
import io.aetera.model.module.ModuleEnrollmentRepository
import io.aetera.model.module.ModuleId
import io.aetera.model.user.UserId
import org.springframework.stereotype.Repository

@Repository
class ModuleEnrollmentRepositoryJpaAdapter(
    private val moduleEnrollmentJpaRepository: ModuleEnrollmentJpaRepository,
) : ModuleEnrollmentRepository {
    override fun save(enrollment: ModuleEnrollment): ModuleEnrollment = moduleEnrollmentJpaRepository
        .saveMerging(
            id = enrollment.id.value,
            update = { it.applyFrom(enrollment) },
            create = { ModuleEnrollmentJpaEntity.from(enrollment) },
        ).toModel()

    override fun getByUserIdAndModuleId(
        userId: UserId,
        moduleId: ModuleId,
    ): ModuleEnrollment? = moduleEnrollmentJpaRepository.findByUserIdAndModuleId(userId.value, moduleId.value)?.toModel()

    override fun findAllByUserId(userId: UserId): List<ModuleEnrollment> =
        moduleEnrollmentJpaRepository.findAllByUserId(userId.value).map { it.toModel() }

    override fun existsEnabled(
        userId: UserId,
        moduleId: ModuleId,
    ): Boolean = moduleEnrollmentJpaRepository.existsByUserIdAndModuleIdAndStatus(
        userId.value,
        moduleId.value,
        EnrollmentStatus.ENABLED,
    )
}
