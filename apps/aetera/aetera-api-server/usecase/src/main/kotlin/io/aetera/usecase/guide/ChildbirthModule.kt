package io.aetera.usecase.guide

import io.aetera.model.guide.GuideModule
import io.aetera.model.guide.GuideTemplate
import org.springframework.stereotype.Component

@Component
class ChildbirthModule : GuideModule {
    override val template: GuideTemplate = CHILDBIRTH_GUIDE
}
