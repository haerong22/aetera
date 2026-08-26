package io.aetera.model.module

import io.aetera.model.user.UserId
import java.time.Instant

/**
 * 사용자 한 명의 모듈 하나에 대한 사용 상태. `(userId, moduleId)` 당 최대 한 행.
 *
 * 비활성화는 소프트다 — 모듈이 쌓은 데이터는 그대로 두고 접근만 막는다.
 * 다시 활성화하면 이전 데이터가 그대로 돌아온다. 데이터 완전 삭제는 별도 기능으로 다룬다.
 */
class ModuleEnrollment private constructor(
    val id: ModuleEnrollmentId,
    val userId: UserId,
    val moduleId: ModuleId,
    status: EnrollmentStatus,
    enabledAt: Instant,
    disabledAt: Instant?,
    sortOrder: Int,
) {
    var status: EnrollmentStatus = status
        private set

    var enabledAt: Instant = enabledAt
        private set

    var disabledAt: Instant? = disabledAt
        private set

    /**
     * 사이드바에 놓이는 순서. 작을수록 위다.
     *
     * 켜짐/꺼짐과 같은 "내 모듈 구성"이라 같은 행에 둔다 — 기기를 바꿔도 순서가 따라와야 한다.
     */
    var sortOrder: Int = sortOrder
        private set

    fun changeSortOrder(sortOrder: Int) {
        this.sortOrder = sortOrder
    }

    val isEnabled: Boolean get() = status == EnrollmentStatus.ENABLED

    /** 이미 활성 상태면 아무 일도 하지 않는다 — 활성화는 몇 번을 눌러도 같은 결과여야 한다. */
    fun enable(at: Instant) {
        if (isEnabled) return
        status = EnrollmentStatus.ENABLED
        enabledAt = at
        disabledAt = null
    }

    fun disable(at: Instant) {
        if (!isEnabled) return
        status = EnrollmentStatus.DISABLED
        disabledAt = at
    }

    override fun equals(other: Any?): Boolean = this === other || (other is ModuleEnrollment && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "ModuleEnrollment(userId=$userId, moduleId=$moduleId, status=$status)"

    companion object {
        /** 순서를 정한 적 없는 모듈은 뒤로 보낸다 — 새로 켠 모듈이 기존 배치를 흔들지 않는다. */
        const val DEFAULT_SORT_ORDER: Int = 1_000

        fun enable(
            id: ModuleEnrollmentId,
            userId: UserId,
            moduleId: ModuleId,
            at: Instant,
        ): ModuleEnrollment = ModuleEnrollment(
            id = id,
            userId = userId,
            moduleId = moduleId,
            status = EnrollmentStatus.ENABLED,
            enabledAt = at,
            disabledAt = null,
            sortOrder = DEFAULT_SORT_ORDER,
        )

        /**
         * 켠 적 없는 모듈의 순서만 정해 둔다.
         *
         * [enable] 로 만들었다가 곧바로 [disable] 하면 켠 적 없는 모듈에 사용 이력이 남는다.
         * "한 번도 안 켰다"와 "켰다가 껐다"는 다른 사실이므로 생성 경로를 나눈다.
         */
        fun forOrder(
            id: ModuleEnrollmentId,
            userId: UserId,
            moduleId: ModuleId,
            at: Instant,
            sortOrder: Int,
        ): ModuleEnrollment = ModuleEnrollment(
            id = id,
            userId = userId,
            moduleId = moduleId,
            status = EnrollmentStatus.DISABLED,
            enabledAt = at,
            disabledAt = null,
            sortOrder = sortOrder,
        )

        fun reconstitute(
            id: ModuleEnrollmentId,
            userId: UserId,
            moduleId: ModuleId,
            status: EnrollmentStatus,
            enabledAt: Instant,
            disabledAt: Instant?,
            sortOrder: Int,
        ): ModuleEnrollment = ModuleEnrollment(id, userId, moduleId, status, enabledAt, disabledAt, sortOrder)
    }
}
