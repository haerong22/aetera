package io.aetera.controller.renewal

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class RenewReq(
    /** 직접 고른 다음 만기일. 없으면 주기가 정한 날짜로 굴린다. */
    @field:Schema(example = "2027-09-30")
    val nextExpiresAt: LocalDate? = null,
)
