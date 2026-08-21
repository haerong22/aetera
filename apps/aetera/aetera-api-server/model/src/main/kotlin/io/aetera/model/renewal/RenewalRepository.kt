package io.aetera.model.renewal

import io.aetera.model.user.UserId

interface RenewalRepository {
    fun save(renewal: Renewal): Renewal

    fun getById(id: RenewalId): Renewal?

    /** 만기가 이른 것부터. 화면 하나가 이 호출 한 번으로 그려진다. */
    fun findAllByUserId(userId: UserId): List<Renewal>

    fun delete(renewal: Renewal)
}
