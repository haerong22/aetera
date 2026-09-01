package io.aetera.usecase.asset

import io.aetera.model.asset.AssetEntryRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 자산 화면 조립. 조회 API 이자 **모든 변경 API 의 응답을 만드는 곳**이기도 하다 —
 * 한 달을 다시 쓰면 순자산도 증감도 추이도 함께 움직인다.
 */
@Service
@Transactional(readOnly = true)
class FindAssetsService(
    private val assetEntryRepository: AssetEntryRepository,
) {
    fun findAssets(userId: UUID): AssetBoardDto = AssetBoardDto.of(assetEntryRepository.findAllByUserId(UserId(userId)))
}
