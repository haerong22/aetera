package io.aetera.model.renewal

import io.aetera.model.user.UserId
import java.time.LocalDate

interface RenewalRepository {
    fun save(renewal: Renewal): Renewal

    fun getById(id: RenewalId): Renewal?

    /** 만기가 이른 것부터. 화면 하나가 이 호출 한 번으로 그려진다. */
    fun findAllByUserId(userId: UserId): List<Renewal>

    /**
     * 만기가 그 기간에 드는 것만. 양 끝을 포함한다.
     *
     * 타임라인처럼 한 해치만 보는 쪽이 쓴다. `(user_id, expires_at)` 인덱스가 받친다.
     */
    fun findAllByUserIdAndExpiresAtBetween(
        userId: UserId,
        from: LocalDate,
        to: LocalDate,
    ): List<Renewal>

    fun delete(renewal: Renewal)
}
