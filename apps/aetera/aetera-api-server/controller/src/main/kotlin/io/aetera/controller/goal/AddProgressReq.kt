package io.aetera.controller.goal

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class AddProgressReq(
    /** 음수면 되돌린다. 잘못 눌렀을 때 취소할 수 있어야 한다. */
    @field:Min(-100_000)
    @field:Max(100_000)
    @field:Schema(example = "1")
    val amount: Int = 1,
)
