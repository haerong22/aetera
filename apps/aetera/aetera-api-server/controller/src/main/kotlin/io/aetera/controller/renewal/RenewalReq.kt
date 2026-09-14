package io.aetera.controller.renewal

import io.aetera.model.renewal.Renewal
import io.aetera.model.renewal.RenewalCategory
import io.aetera.model.renewal.RenewalCycle
import io.aetera.usecase.renewal.cmd.SaveRenewalCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class RenewalReq(
    @field:Schema(example = "자동차보험")
    val title: String,
    val category: RenewalCategory = RenewalCategory.ETC,
    @field:Schema(example = "2027-03-15")
    val expiresAt: LocalDate,
    val cycle: RenewalCycle = RenewalCycle.YEARLY,
    val noticeDays: Int = Renewal.DEFAULT_NOTICE_DAYS,
    val memo: String? = null,
) {
    fun toCommand(userId: UUID): SaveRenewalCommand = SaveRenewalCommand(
        userId = userId,
        title = title,
        category = category,
        expiresAt = expiresAt,
        cycle = cycle,
        noticeDays = noticeDays,
        memo = memo,
    )
}
