package io.aetera.usecase.guide

import io.aetera.model.guide.GuideLink
import io.aetera.model.guide.GuideTask
import io.aetera.model.guide.GuideTaskKey

/**
 * 둘 이상의 가이드가 함께 쓰는 공식 창구.
 *
 * 한 가이드에서만 쓰는 링크는 그 콘텐츠 파일에 둔다 — 전부 여기 모으면 어느 가이드가
 * 무엇을 쓰는지 알려면 파일 두 개를 오가야 한다. 겹치는 것만 올라온다.
 */
internal object SharedLinks {
    val HOMETAX = GuideLink("국세청 홈택스", "https://www.hometax.go.kr")
}

internal fun task(
    key: String,
    title: String,
    description: String,
    dueOffsetDays: Int,
    required: Boolean = true,
    link: GuideLink? = null,
) = GuideTask(GuideTaskKey(key), title, description, dueOffsetDays, required, link)
