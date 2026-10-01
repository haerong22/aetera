package io.aetera.model.spending

import io.aetera.model.user.UserId
import java.time.LocalDate

interface SpendingRecordRepository {
    fun save(record: SpendingRecord): SpendingRecord

    /** 최근 달부터. 화면 하나가 이 호출 한 번으로 그려진다. */
    fun findAllByUserId(userId: UserId): List<SpendingRecord>

    /**
     * 그 달의 기록. 한 달에 하나뿐이라 목록이 아니다.
     *
     * 저장이 **덮어쓰기**이므로 먼저 이걸로 찾는다 — 같은 달을 두 번 적으면 줄이 둘 생기는
     * 것이 아니라 뒤에 적은 값이 남아야 한다. 유니크 인덱스가 그걸 받친다.
     */
    fun findByUserIdAndMonth(
        userId: UserId,
        month: LocalDate,
    ): SpendingRecord?

    /**
     * 그 기간에 속한 달만. 양 끝을 포함한다.
     *
     * 타임라인처럼 한 해치만 보는 쪽이 쓴다 — 전부 읽어 와서 메모리에서 자르면 몇 해씩
     * 기록한 사람의 표를 매번 통째로 읽는다. `(user_id, month)` 유니크 인덱스가 받친다.
     */
    fun findAllByUserIdAndMonthBetween(
        userId: UserId,
        from: LocalDate,
        to: LocalDate,
    ): List<SpendingRecord>

    /** 탈퇴할 때 한 번. 이 사용자의 것을 통째로 지운다. */
    fun deleteAllByUserId(userId: UserId)

    fun deleteByUserIdAndMonth(
        userId: UserId,
        month: LocalDate,
    )
}
