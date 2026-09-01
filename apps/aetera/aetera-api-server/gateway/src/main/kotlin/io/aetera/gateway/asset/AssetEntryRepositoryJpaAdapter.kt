package io.aetera.gateway.asset

import io.aetera.model.asset.AssetEntry
import io.aetera.model.asset.AssetEntryRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * 한 줄이 한 번 쓰이고 다시 고쳐지지 않으므로 `saveMerging` 을 쓰지 않는다 —
 * 스냅샷 수정은 그 달을 지우고 새로 넣는 것이라 언제나 새 행이다.
 */
@Repository
class AssetEntryRepositoryJpaAdapter(
    private val assetEntryJpaRepository: AssetEntryJpaRepository,
) : AssetEntryRepository {
    override fun saveAll(entries: List<AssetEntry>): List<AssetEntry> = assetEntryJpaRepository
        .saveAll(entries.map(AssetEntryJpaEntity::from))
        .map { it.toModel() }

    override fun findAllByUserId(userId: UserId): List<AssetEntry> = assetEntryJpaRepository
        .findAllByUserIdOrderByMonthDesc(userId.value)
        .map { it.toModel() }

    override fun deleteByUserIdAndMonth(
        userId: UserId,
        month: LocalDate,
    ) {
        assetEntryJpaRepository.deleteByUserIdAndMonth(userId.value, month)
    }
}
