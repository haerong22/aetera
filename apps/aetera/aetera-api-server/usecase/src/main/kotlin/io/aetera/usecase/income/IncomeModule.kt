package io.aetera.usecase.income

import io.aetera.model.module.AeteraModule
import io.aetera.model.module.ModuleCategory
import io.aetera.model.module.ModuleDescriptor
import io.aetera.model.module.ModuleId
import org.springframework.stereotype.Component

@Component
class IncomeModule : AeteraModule {
    override val descriptor: ModuleDescriptor =
        ModuleDescriptor(
            id = MODULE_ID,
            displayName = "소득",
            description = "매달 들어오는 돈을 모아 두면, 고정지출과 견줘 얼마가 남는지 보여요.",
            category = ModuleCategory.TOOL,
        )

    companion object {
        val MODULE_ID: ModuleId = ModuleId("income")
    }
}
