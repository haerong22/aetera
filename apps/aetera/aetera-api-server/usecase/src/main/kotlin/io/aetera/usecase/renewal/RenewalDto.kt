package io.aetera.usecase.renewal

import io.aetera.model.renewal.Renewal
import io.aetera.model.renewal.RenewalCategory
import io.aetera.model.renewal.RenewalCycle
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * 만기 항목 하나.
 *
 * "지났는지·임박했는지"는 담지 않는다. 그건 사용자의 로컬 날짜를 알아야 하는데 서버는 그걸 모른다.
 * [expiresAt] 과 [noticeDays] 만 주면 브라우저가 자기 달력으로 판단하므로 언제나 맞다.
 */
data class RenewalDto(
    val id: UUID,
    val title: String,
    val category: RenewalCategory,
    val expiresAt: LocalDate,
    val cycle: RenewalCycle,
    val noticeDays: Int,
    val memo: String?,
    /** 갱신했을 때 잡히는 날짜. 주기가 없으면 null. 화면이 모달 기본값으로 쓴다. */
    val nextExpiresAt: LocalDate?,
    val createdAt: Instant,
) {
    constructor(renewal: Renewal, today: LocalDate) : this(
        id = renewal.id.value,
        title = renewal.title,
        category = renewal.category,
        expiresAt = renewal.expiresAt,
        cycle = renewal.cycle,
        noticeDays = renewal.noticeDays,
        memo = renewal.memo,
        nextExpiresAt = renewal.cycle.takeIf { it.repeats }?.let { renewal.nextExpiryFrom(today) },
        createdAt = renewal.createdAt,
    )
}
