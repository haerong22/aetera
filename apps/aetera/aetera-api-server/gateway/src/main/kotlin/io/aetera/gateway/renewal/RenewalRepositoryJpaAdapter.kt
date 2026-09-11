package io.aetera.gateway.renewal

import io.aetera.gateway.common.saveMerging
import io.aetera.model.renewal.Renewal
import io.aetera.model.renewal.RenewalId
import io.aetera.model.renewal.RenewalRepository
import io.aetera.model.user.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class RenewalRepositoryJpaAdapter(
    private val renewalJpaRepository: RenewalJpaRepository,
) : RenewalRepository {
    override fun save(renewal: Renewal): Renewal = renewalJpaRepository
        .saveMerging(
            id = renewal.id.value,
            update = { it.applyFrom(renewal) },
            create = { RenewalJpaEntity.from(renewal) },
        ).toModel()

    override fun getById(id: RenewalId): Renewal? = renewalJpaRepository.findByIdOrNull(id.value)?.toModel()

    override fun findAllByUserId(userId: UserId): List<Renewal> = renewalJpaRepository
        .findAllByUserId(userId.value)
        .map { it.toModel() }

    override fun findAllByUserIdAndExpiresAtBetween(
        userId: UserId,
        from: LocalDate,
        to: LocalDate,
    ): List<Renewal> = renewalJpaRepository
        .findAllByUserIdAndExpiresAtBetween(userId.value, from, to)
        .map { it.toModel() }

    override fun delete(renewal: Renewal) {
        renewalJpaRepository.deleteById(renewal.id.value)
    }
}
