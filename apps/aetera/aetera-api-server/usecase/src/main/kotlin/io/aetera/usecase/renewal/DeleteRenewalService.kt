package io.aetera.usecase.renewal

import io.aetera.model.renewal.RenewalErrorCode
import io.aetera.model.renewal.RenewalId
import io.aetera.model.renewal.RenewalRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.common.orNotFound
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
        val id = RenewalId(renewalId)
        val renewal =
            renewalRepository
                .getById(id)
                .orNotFound(UserId(userId), RenewalErrorCode.RENEWAL_NOT_FOUND, id)
        renewalRepository.delete(renewal)
    }
}
