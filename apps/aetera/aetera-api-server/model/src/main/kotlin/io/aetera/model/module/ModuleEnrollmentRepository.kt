package io.aetera.model.module

import io.aetera.model.user.UserId

interface ModuleEnrollmentRepository {
    fun save(enrollment: ModuleEnrollment): ModuleEnrollment

    /** 탈퇴할 때 한 번. 이 사용자의 것을 통째로 지운다. */
    fun deleteAllByUserId(userId: UserId)

    fun getByUserIdAndModuleId(
        userId: UserId,
        moduleId: ModuleId,
    ): ModuleEnrollment?

    fun findAllByUserId(userId: UserId): List<ModuleEnrollment>

    /** 활성화 가드가 요청마다 부르므로 애그리거트를 다 읽지 않고 존재만 확인한다. */
    fun existsEnabled(
        userId: UserId,
        moduleId: ModuleId,
    ): Boolean
}
