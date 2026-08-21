package io.aetera.usecase.renewal

import io.aetera.model.renewal.RenewalId
import io.aetera.model.renewal.RenewalRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteRenewalService(
    private val renewalRepository: RenewalRepository,
) {
    @Transactional
    fun delete(
        userId: UUID,
        renewalId: UUID,
    ) {
        renewalRepository.delete(renewalRepository.getOwnedOrThrow(RenewalId(renewalId), UserId(userId)))
    }
}
