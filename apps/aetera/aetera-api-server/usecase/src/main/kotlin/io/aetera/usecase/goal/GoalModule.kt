package io.aetera.usecase.goal

import io.aetera.model.module.AeteraModule
import io.aetera.model.module.ModuleCategory
import io.aetera.model.module.ModuleDescriptor
import io.aetera.model.module.ModuleId
import org.springframework.stereotype.Component

@Component
class GoalModule : AeteraModule {
    override val descriptor: ModuleDescriptor =
        ModuleDescriptor(
            id = MODULE_ID,
            displayName = "목표",
            description = "이번 주에 뭘 하기로 했는지 정하고 진행을 눈으로 봐요.",
            category = ModuleCategory.TOOL,
        )

    companion object {
        val MODULE_ID: ModuleId = ModuleId("goal")
    }
}
