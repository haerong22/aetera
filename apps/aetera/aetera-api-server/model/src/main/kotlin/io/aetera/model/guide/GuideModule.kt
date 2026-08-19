package io.aetera.model.guide

import io.aetera.model.module.AeteraModule
import io.aetera.model.module.ModuleCategory
import io.aetera.model.module.ModuleDescriptor

interface GuideModule : AeteraModule {
    val template: GuideTemplate

    override val descriptor: ModuleDescriptor
        get() =
            ModuleDescriptor(
                id = template.id.toModuleId(),
                displayName = template.title,
                description = template.summary,
                category = ModuleCategory.GUIDE,
            )
}
