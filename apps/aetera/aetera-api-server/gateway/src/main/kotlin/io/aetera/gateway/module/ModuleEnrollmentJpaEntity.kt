package io.aetera.gateway.module

import io.aetera.gateway.common.UuidJpaEntity
import io.aetera.model.module.EnrollmentStatus
import io.aetera.model.module.ModuleEnrollment
import io.aetera.model.module.ModuleEnrollmentId
import io.aetera.model.module.ModuleId
import io.aetera.model.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "module_enrollments")
class ModuleEnrollmentJpaEntity(
    uid: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "module_id", nullable = false, length = 50, updatable = false)
    val moduleId: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: EnrollmentStatus,
    @Column(name = "enabled_at", nullable = false)
    var enabledAt: Instant,
    @Column(name = "disabled_at")
    var disabledAt: Instant?,
) : UuidJpaEntity(uid) {
    fun applyFrom(enrollment: ModuleEnrollment) {
        status = enrollment.status
        enabledAt = enrollment.enabledAt
        disabledAt = enrollment.disabledAt
    }

    fun toModel(): ModuleEnrollment = ModuleEnrollment.reconstitute(
        id = ModuleEnrollmentId(uid),
        userId = UserId(userId),
        moduleId = ModuleId(moduleId),
        status = status,
        enabledAt = enabledAt,
        disabledAt = disabledAt,
    )

    companion object {
        fun from(enrollment: ModuleEnrollment): ModuleEnrollmentJpaEntity = ModuleEnrollmentJpaEntity(
            uid = enrollment.id.value,
            userId = enrollment.userId.value,
            moduleId = enrollment.moduleId.value,
            status = enrollment.status,
            enabledAt = enrollment.enabledAt,
            disabledAt = enrollment.disabledAt,
        )
    }
}
