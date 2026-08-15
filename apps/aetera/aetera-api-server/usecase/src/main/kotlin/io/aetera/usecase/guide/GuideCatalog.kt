package io.aetera.usecase.guide

import io.aetera.model.guide.GuideErrorCode
import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideModule
import io.aetera.model.guide.GuideTemplate
import io.aetera.shared.error.CoreException
import org.springframework.stereotype.Component

/**
 * 배포된 가이드 콘텐츠의 목록. [GuideModule] 빈을 전부 주입받아 만들어지므로
 * **새 가이드를 추가해도 이 클래스와 서비스들은 바뀌지 않는다.**
 *
 * 코어의 `ModuleRegistry` 와 같은 방식이되 대상이 다르다: 저쪽은 "배포된 모듈",
 * 여기는 "그중 가이드형 모듈의 콘텐츠".
 */
@Component
class GuideCatalog(
    guides: List<GuideModule>,
) {
    private val byId: Map<GuideId, GuideTemplate> = guides.associate { it.template.id to it.template }

    /**
     * 모듈 가드가 이미 "배포됐고 사용자가 켠 모듈"임을 보장한 뒤에 불린다.
     * 그래도 여기서 한 번 더 막는 이유: 가이드가 아닌 모듈(일정)의 아이디로 이 API 를 부르면
     * 403 이 아니라 404 여야 하기 때문이다.
     */
    fun getOrThrow(id: GuideId): GuideTemplate = byId[id] ?: throw CoreException(GuideErrorCode.GUIDE_NOT_FOUND, "존재하지 않는 가이드입니다. id=$id")
}
