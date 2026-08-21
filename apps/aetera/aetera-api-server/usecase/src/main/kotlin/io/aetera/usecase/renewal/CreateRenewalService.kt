package io.aetera.usecase.renewal

import io.aetera.model.renewal.Renewal
import io.aetera.model.renewal.RenewalId
import io.aetera.model.renewal.RenewalRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.today
import io.aetera.usecase.renewal.cmd.SaveRenewalCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CreateRenewalService(
    private val renewalRepository: RenewalRepository,
    private val clock: Clock,
) {
    @Transactional
    fun create(command: SaveRenewalCommand): RenewalDto {
        val now = clock.instant()
        val renewal =
            Renewal.create(
                id = RenewalId.next(),
                userId = UserId(command.userId),
                title = command.title,
                category = command.category,
                expiresAt = command.expiresAt,
                cycle = command.cycle,
                noticeDays = command.noticeDays,
                memo = command.memo,
                today = clock.today(),
                createdAt = now,
            )
        return RenewalDto(renewalRepository.save(renewal), clock.today())
    }
}
