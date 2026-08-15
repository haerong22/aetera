package io.aetera.model.guide

import io.aetera.model.module.ModuleId

/**
 * 가이드의 정체성. 가이드 하나가 곧 모듈 하나이므로 [ModuleId] 와 같은 값을 쓴다
 * (`resignation` 가이드 = `resignation` 모듈). 그래서 URL 한 조각으로 둘 다 가리킬 수 있고,
 * 코어 모듈 가드가 이미 걸러 준 값을 가이드 카탈로그가 그대로 받는다.
 */
@JvmInline
value class GuideId(
    val value: String,
) {
    fun toModuleId(): ModuleId = ModuleId(value)

    override fun toString(): String = value
}
