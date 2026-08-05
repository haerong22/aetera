package io.aetera.model.module

/**
 * 모듈의 성격. 계약은 같지만 화면에서 다르게 분류해 보여준다.
 */
enum class ModuleCategory {
    /** 일정, 가계부처럼 사용자가 데이터를 쌓아 가는 도구형 모듈. */
    TOOL,

    /** 퇴사 준비, 결혼 준비처럼 콘텐츠와 체크리스트가 이끄는 가이드형 모듈. */
    GUIDE,
}
