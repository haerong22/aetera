package io.aetera.usecase.module

import io.aetera.model.module.ModuleEnrollment
import io.aetera.model.module.ModuleEnrollmentId
import io.aetera.model.module.ModuleEnrollmentRepository
import io.aetera.model.module.ModuleId
import io.aetera.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class ReorderModulesService(
    private val moduleRegistry: ModuleRegistry,
    private val moduleEnrollmentRepository: ModuleEnrollmentRepository,
    private val clock: Clock,
) {
    /**
     * 받은 목록 순서대로 다시 매긴다.
     *
     * 위치 하나만 옮기는 API 로 두면 서버가 나머지를 어떻게 밀지 정해야 하고, 그 규칙이
     * 화면이 보여 준 결과와 어긋나면 순서가 튄다. 화면이 이미 계산한 최종 순서를 그대로 받는다.
     *
     * 목록에 없는 모듈은 건드리지 않는다 — 화면이 일부만 보내도 나머지 배치가 무너지지 않는다.
     */
    @Transactional
    fun reorder(
        userId: UUID,
        moduleIds: List<String>,
    ): List<ModuleSummaryDto> {
        val owner = UserId(userId)
        val enrollments =
            moduleEnrollmentRepository.findAllByUserId(owner).associateByTo(mutableMapOf()) { it.moduleId }

        // 같은 모듈이 두 번 오면 아직 행이 없는 모듈에 행을 두 개 만들려다 유니크 제약에 걸린다.
        // 그 실패는 "다시 시도하세요"로 나가는데 다시 보내도 결과가 같으므로, 접어서 받는다.
        moduleIds.distinct().forEachIndexed { index, rawId ->
            val id = ModuleId(rawId)
            moduleRegistry.getOrThrow(id)

            val enrollment =
                enrollments[id]?.apply { changeSortOrder(index) }
                    // 아직 켠 적 없는 모듈도 순서는 정할 수 있어야 한다. 켜짐 상태는 건드리지 않는다.
                    ?: ModuleEnrollment.forOrder(ModuleEnrollmentId.next(), owner, id, clock.instant(), index)
            enrollments[id] = moduleEnrollmentRepository.save(enrollment)
        }

        // 저장이 돌려준 값을 그대로 쓴다 — 방금 쓴 것을 다시 읽을 이유가 없다.
        return moduleRegistry.descriptors.toSidebarOrder(enrollments)
    }
}
