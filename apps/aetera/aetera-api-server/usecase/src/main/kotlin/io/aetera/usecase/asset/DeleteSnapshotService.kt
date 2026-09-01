package io.aetera.usecase.asset

import io.aetera.model.asset.AssetEntry
import io.aetera.model.asset.AssetEntryRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class DeleteSnapshotService(
    private val assetEntryRepository: AssetEntryRepository,
    private val findAssetsService: FindAssetsService,
) {
    /**
     * 그 달의 기록을 지운다. 없는 달을 지워도 조용히 성공한다 —
     * 지우기는 몇 번을 눌러도 같은 결과여야 하고, "없다"는 것도 원하던 상태다.
     *
     * 소유자 검사를 따로 하지 않는 이유: 지우는 조건에 `userId` 가 들어가 있어
     * 남의 달을 지정해도 자기 행만 지워진다.
     */
    @Transactional
    fun delete(
        userId: UUID,
        month: LocalDate,
    ): AssetBoardDto {
        assetEntryRepository.deleteByUserIdAndMonth(UserId(userId), AssetEntry.normalizeMonth(month))
        return findAssetsService.findAssets(userId)
    }
}
