package io.aetera.usecase.renewal

import io.aetera.model.renewal.RenewalErrorCode
import io.aetera.model.renewal.RenewalId
import io.aetera.model.renewal.RenewalRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.orNotFound
import io.aetera.usecase.common.today
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

@Service
class RenewRenewalService(
    private val renewalRepository: RenewalRepository,
    private val clock: Clock,
) {
    /** 갱신했다고 표시하면 다음 만기로 굴러간다. 이 모듈이 일정과 갈리는 지점이다. */
    @Transactional
    fun renew(
        userId: UUID,
        renewalId: UUID,
        nextExpiresAt: LocalDate?,
    ): RenewalDto {
        val today = clock.today()
        val id = RenewalId(renewalId)
        val renewal = renewalRepository.getById(id).orNotFound(UserId(userId), RenewalErrorCode.RENEWAL_NOT_FOUND, id)

        // 날짜를 직접 고르면 그 값으로, 아니면 주기가 정한 다음 만기로 굴린다.
        if (nextExpiresAt == null) {
            renewal.renew(today)
        } else {
            renewal.update(
                title = renewal.title,
                category = renewal.category,
                expiresAt = nextExpiresAt,
                cycle = renewal.cycle,
                noticeDays = renewal.noticeDays,
                memo = renewal.memo,
                today = today,
            )
        }
        return RenewalDto(renewalRepository.save(renewal), today)
    }
}
