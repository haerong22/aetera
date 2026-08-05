package io.aetera.usecase.schedule

import io.aetera.model.module.AeteraModule
import io.aetera.model.module.ModuleCategory
import io.aetera.model.module.ModuleDescriptor
import io.aetera.model.module.ModuleId
import org.springframework.stereotype.Component

/**
 * 일정 모듈의 플랫폼 등록. 새 모듈은 이 파일 같은 빈 하나로 플랫폼에 편입된다 —
 * 코어(레지스트리, 가드, 모듈 스토어)는 아무것도 몰라도 된다.
 */
@Component
class ScheduleModule : AeteraModule {
    override val descriptor: ModuleDescriptor =
        ModuleDescriptor(
            id = MODULE_ID,
            displayName = "일정",
            description = "하루의 약속과 할 일을 캘린더로 관리해요.",
            category = ModuleCategory.TOOL,
        )

    companion object {
        val MODULE_ID: ModuleId = ModuleId("schedule")
    }
}
