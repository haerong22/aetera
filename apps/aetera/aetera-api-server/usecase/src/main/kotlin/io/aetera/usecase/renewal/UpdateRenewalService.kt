package io.aetera.usecase.renewal

import io.aetera.model.renewal.RenewalId
import io.aetera.model.renewal.RenewalRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.today
import io.aetera.usecase.renewal.cmd.SaveRenewalCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class UpdateRenewalService(
    private val renewalRepository: RenewalRepository,
    private val clock: Clock,
) {
    @Transactional
    fun update(
        renewalId: UUID,
        command: SaveRenewalCommand,
    ): RenewalDto {
        val renewal = renewalRepository.getOwnedOrThrow(RenewalId(renewalId), UserId(command.userId))
        renewal.update(
            title = command.title,
            category = command.category,
            expiresAt = command.expiresAt,
            cycle = command.cycle,
            noticeDays = command.noticeDays,
            memo = command.memo,
            today = clock.today(),
        )
        return RenewalDto(renewalRepository.save(renewal), clock.today())
    }
}
