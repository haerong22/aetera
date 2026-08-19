package io.aetera.usecase.guide

import io.aetera.model.guide.GuideLink
import io.aetera.model.guide.GuideTask
import io.aetera.model.guide.GuideTaskKey

internal fun task(
    key: String,
    title: String,
    description: String,
    dueOffsetDays: Int,
    required: Boolean = true,
    link: GuideLink? = null,
) = GuideTask(GuideTaskKey(key), title, description, dueOffsetDays, required, link)
