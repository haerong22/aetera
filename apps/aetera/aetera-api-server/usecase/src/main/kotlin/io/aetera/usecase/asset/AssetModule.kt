package io.aetera.usecase.asset

import io.aetera.model.module.AeteraModule
import io.aetera.model.module.ModuleCategory
import io.aetera.model.module.ModuleDescriptor
import io.aetera.model.module.ModuleId
import org.springframework.stereotype.Component

@Component
class AssetModule : AeteraModule {
    override val descriptor: ModuleDescriptor =
        ModuleDescriptor(
            id = MODULE_ID,
            displayName = "자산",
            description = "한 달에 한 번 잔액만 적어 두면, 순자산이 어느 쪽으로 가고 있는지 보여요.",
            category = ModuleCategory.TOOL,
        )

    companion object {
        val MODULE_ID: ModuleId = ModuleId("asset")
    }
}
