package io.aetera.usecase.module

import io.aetera.model.module.AeteraModule
import io.aetera.model.module.ModuleDescriptor
import io.aetera.model.module.ModuleErrorCode
import io.aetera.model.module.ModuleId
import io.aetera.shared.error.CoreException
import org.springframework.stereotype.Component

/**
 * 배포된 모듈의 목록. 모듈이 등록한 [AeteraModule] 빈을 전부 주입받아 만들어지므로
 * **새 모듈을 추가해도 이 클래스는 바뀌지 않는다.**
 *
 * DB 카탈로그를 두지 않는 이유: 모듈은 코드가 배포되어 있어야 존재한다.
 * DB 행으로는 없는 코드를 켤 수 없다.
 */
@Component
class ModuleRegistry(
    modules: List<AeteraModule>,
) {
    private val byId: Map<ModuleId, ModuleDescriptor> =
        modules
            .map { it.descriptor }
            .also { descriptors ->
                val duplicated = descriptors.groupBy { it.id }.filterValues { it.size > 1 }.keys
                check(duplicated.isEmpty()) { "모듈 아이디가 겹칩니다: $duplicated" }
            }.associateBy { it.id }

    // 배포된 모듈은 기동 후 바뀌지 않는다. get() 으로 두면 조회할 때마다 정렬을 다시 한다.
    val descriptors: List<ModuleDescriptor> = byId.values.sortedBy { it.id.value }

    fun getOrThrow(id: ModuleId): ModuleDescriptor =
        byId[id] ?: throw CoreException(ModuleErrorCode.MODULE_NOT_FOUND, "존재하지 않는 모듈입니다. id=$id")
}
