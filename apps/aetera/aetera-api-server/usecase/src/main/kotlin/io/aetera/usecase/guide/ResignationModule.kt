package io.aetera.usecase.guide

import io.aetera.model.guide.GuideModule
import io.aetera.model.guide.GuideTemplate
import io.aetera.model.module.ModuleCategory
import io.aetera.model.module.ModuleDescriptor
import org.springframework.stereotype.Component

/**
 * 퇴사 준비 모듈의 플랫폼 등록.
 *
 * 가이드형 모듈이 갖는 고유한 코드는 이 파일과 [RESIGNATION_GUIDE] 뿐이다 —
 * 여정·진행·마감 계산·화면은 전부 공용이다. 다음 가이드(결혼 준비, 이사)도 같은 두 파일로 붙는다.
 */
@Component
class ResignationModule : GuideModule {
    override val template: GuideTemplate = RESIGNATION_GUIDE

    override val descriptor: ModuleDescriptor =
        ModuleDescriptor(
            id = template.id.toModuleId(),
            displayName = template.title,
            description = template.summary,
            category = ModuleCategory.GUIDE,
        )
}
