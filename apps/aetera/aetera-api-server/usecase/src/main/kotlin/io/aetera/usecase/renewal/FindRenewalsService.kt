package io.aetera.usecase.renewal

import io.aetera.model.renewal.RenewalRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.today
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindRenewalsService(
    private val renewalRepository: RenewalRepository,
    private val clock: Clock,
) {
    fun findRenewals(userId: UUID): List<RenewalDto> {
        val today = clock.today()
        return renewalRepository.findAllByUserId(UserId(userId)).map { RenewalDto(it, today) }
    }
}
