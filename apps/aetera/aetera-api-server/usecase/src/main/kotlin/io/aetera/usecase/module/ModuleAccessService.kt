package io.aetera.usecase.module

import io.aetera.model.module.ModuleEnrollmentRepository
import io.aetera.model.module.ModuleErrorCode
import io.aetera.model.module.ModuleId
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.aetera.shared.error.ensure
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 모듈 접근 가드의 판단 로직. `/api/v1/modules/{module-id}/..` 로 들어오는 모든 요청에 대해
 * 인바운드 어댑터의 인터셉터가 이걸 부른다. 모듈 쪽에는 검사 코드가 한 줄도 없다.
 */
@Service
@Transactional(readOnly = true)
class ModuleAccessService(
    private val moduleRegistry: ModuleRegistry,
    private val moduleEnrollmentRepository: ModuleEnrollmentRepository,
) {
    fun checkAccess(
        userId: UUID,
        moduleId: String,
    ) {
        // 경로에 이상한 값이 들어오는 것은 "형식 오류(400)"가 아니라 "없는 모듈(404)"이다.
        // 형식만 400 으로 내보내면 같은 상황(모르는 경로 조각)에 두 가지 응답이 나간다.
        val id =
            ModuleId.parseOrNull(moduleId)
                ?: throw CoreException(ModuleErrorCode.MODULE_NOT_FOUND, "존재하지 않는 모듈입니다. moduleId=$moduleId")
        moduleRegistry.getOrThrow(id)
        ensure(
            moduleEnrollmentRepository.existsEnabled(UserId(userId), id),
            ModuleErrorCode.MODULE_NOT_ENABLED,
            "사용 설정하지 않은 모듈입니다. moduleId=$id",
        )
    }
}
