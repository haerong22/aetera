package io.aetera.usecase.guide

import io.aetera.model.guide.GuideModule
import io.aetera.model.guide.GuideTemplate
import org.springframework.stereotype.Component

@Component
class YearEndTaxModule : GuideModule {
    override val template: GuideTemplate = YEAR_END_TAX_GUIDE
}
