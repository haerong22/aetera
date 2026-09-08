package io.aetera.usecase.timeline

import io.aetera.model.module.AeteraModule
import io.aetera.model.module.ModuleCategory
import io.aetera.model.module.ModuleDescriptor
import io.aetera.model.module.ModuleId
import org.springframework.stereotype.Component

@Component
class TimelineModule : AeteraModule {
    override val descriptor: ModuleDescriptor =
        ModuleDescriptor(
            id = MODULE_ID,
            displayName = "타임라인",
            description = "켠 모듈들이 남긴 기록을 한 줄로 모아, 지나온 길을 시간순으로 보여줘요.",
            category = ModuleCategory.TOOL,
        )

    companion object {
        val MODULE_ID: ModuleId = ModuleId("timeline")
    }
}
