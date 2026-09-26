package io.aetera.model.goal

import io.aetera.model.user.UserId

interface GoalRepository {
    fun save(goal: Goal): Goal

    fun getById(id: GoalId): Goal?

    /** 만든 순서대로. 화면 하나가 이 호출 한 번으로 그려진다. */
    fun findAllByUserId(userId: UserId): List<Goal>

    /** 탈퇴할 때 한 번. 이 사용자의 것을 통째로 지운다. */
    fun deleteAllByUserId(userId: UserId)

    fun delete(goal: Goal)
}
