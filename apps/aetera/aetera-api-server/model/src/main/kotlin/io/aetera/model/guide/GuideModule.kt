package io.aetera.model.guide

import io.aetera.model.module.AeteraModule

/**
 * 가이드형 모듈이 자기 콘텐츠를 플랫폼에 내놓는 계약.
 *
 * 가이드는 "엔진 + 콘텐츠"로 갈린다. 여정·진행·마감 계산은 전부 공용 엔진이 하고,
 * 모듈이 가진 고유한 것은 [template] 하나뿐이다. 그래서 두 번째 가이드(결혼 준비, 이사)는
 * **템플릿 하나와 이 인터페이스를 구현한 빈 하나**로 끝난다 — 엔티티도, 테이블도, 화면도 늘지 않는다.
 *
 * [AeteraModule] 을 상속하므로 코어의 모듈 레지스트리·가드·스토어에는 그대로 편입된다.
 */
interface GuideModule : AeteraModule {
    val template: GuideTemplate
}
