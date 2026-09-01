package io.aetera.usecase.asset

import io.aetera.model.asset.AssetEntry
import io.aetera.model.asset.AssetEntryId
import io.aetera.model.asset.AssetEntryRepository
import io.aetera.model.asset.AssetErrorCode
import io.aetera.model.user.UserId
import io.aetera.shared.error.ensure
import io.aetera.usecase.asset.cmd.SaveSnapshotCommand
import io.aetera.usecase.common.today
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class SaveSnapshotService(
    private val assetEntryRepository: AssetEntryRepository,
    private val findAssetsService: FindAssetsService,
    private val clock: Clock,
) {
    /**
     * 그 달을 통째로 갈아 끼운다. 같은 달로 몇 번을 보내도 결과가 같다.
     *
     * 지우고 다시 넣는 이유: 스냅샷은 "그 달의 상태 전체"다. 줄 단위로 맞춰 넣으려면
     * 무엇이 사라졌는지 무엇이 새로 생겼는지 서버가 짐작해야 하고, 그 짐작이 틀리면
     * 지난 달 잔액이 이번 달에 유령처럼 남는다.
     */
    @Transactional
    fun save(command: SaveSnapshotCommand): AssetBoardDto {
        ensure(
            command.entries.size <= AssetEntry.MAX_ENTRIES_PER_MONTH,
            AssetErrorCode.TOO_MANY_ENTRIES,
            "한 달에 담을 수 있는 항목은 ${AssetEntry.MAX_ENTRIES_PER_MONTH}개까지입니다. 입력: ${command.entries.size}",
        )

        val owner = UserId(command.userId)
        val today = clock.today()
        val recordedAt = clock.instant()
        val month = AssetEntry.normalizeMonth(command.month)

        // 만들기부터 한다 — 한 줄이라도 형식이 틀리면 지우기 전에 멈춘다.
        val entries =
            command.entries.map {
                AssetEntry.create(
                    id = AssetEntryId.next(),
                    userId = owner,
                    month = month,
                    name = it.name,
                    category = it.category,
                    amount = it.amount,
                    today = today,
                    recordedAt = recordedAt,
                )
            }

        assetEntryRepository.deleteByUserIdAndMonth(owner, month)
        assetEntryRepository.saveAll(entries)
        return findAssetsService.findAssets(command.userId)
    }
}
