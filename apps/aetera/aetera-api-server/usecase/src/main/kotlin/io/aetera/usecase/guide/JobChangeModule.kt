package io.aetera.usecase.guide

import io.aetera.model.guide.GuideModule
import io.aetera.model.guide.GuideTemplate
import org.springframework.stereotype.Component

@Component
class JobChangeModule : GuideModule {
    override val template: GuideTemplate = JOB_CHANGE_GUIDE
}
