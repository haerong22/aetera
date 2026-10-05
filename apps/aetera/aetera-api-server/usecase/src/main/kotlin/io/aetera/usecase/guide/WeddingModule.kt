package io.aetera.usecase.guide

import io.aetera.model.guide.GuideModule
import io.aetera.model.guide.GuideTemplate
import org.springframework.stereotype.Component

@Component
class WeddingModule : GuideModule {
    override val template: GuideTemplate = WEDDING_GUIDE
}
