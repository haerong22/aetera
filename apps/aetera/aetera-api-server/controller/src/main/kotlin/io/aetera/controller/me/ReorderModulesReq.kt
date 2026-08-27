package io.aetera.controller.me

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class ReorderModulesReq(
    /** 사이드바에 놓일 순서. 앞이 위다. */
    @field:NotEmpty
    @field:Size(max = 100)
    @field:Schema(example = "[\"schedule\", \"goal\", \"renewal\"]")
    val moduleIds: List<String>,
)
