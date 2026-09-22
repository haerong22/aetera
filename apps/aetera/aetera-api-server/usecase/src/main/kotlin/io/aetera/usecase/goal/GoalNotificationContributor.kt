package io.aetera.usecase.goal

import io.aetera.model.goal.Goal
import io.aetera.model.goal.GoalRepository
import io.aetera.model.module.ModuleId
import io.aetera.model.module.Notice
import io.aetera.model.module.NotificationContributor
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 주기가 끝나기 전에 못 채운 목표를 알린다.
 *
 * **이 기여자만 미리 알린다.** 만기와 가이드는 지났을 때 재촉하면 되지만, 목표는 주기가
 * 넘어가는 순간 진행도가 0 으로 돌아가 재촉할 것 자체가 사라진다 — 지나고 나서 말해 주면
 * "지난주에 못 했네요"라는 쓸모없는 소식이 된다.
 *
 * 그래서 **마지막 [NOTICE_DAYS] 일**에만 낸다. 주간 목표면 토요일부터, 월간이면 월말 이틀 전부터다.
 * 더 일찍 보내면 주중 내내 "아직 1/3" 이 오고, 그러면 목표를 켠 사람은 매일 메일을 받는다.
 *
 * 이룬 목표는 내지 않는다. 다 한 사람에게 보내는 알림은 재촉이 아니라 잡음이다.
 */
@Component
class GoalNotificationContributor(
    private val goalRepository: GoalRepository,
) : NotificationContributor {
    private companion object {
        /** 주기가 끝나기 며칠 전부터 알릴지. 주간 목표의 주말 이틀이 기준이다. */
        const val NOTICE_DAYS = 1L
    }

    override val moduleIds: Set<ModuleId> = setOf(GoalModule.MODULE_ID)

    override fun noticesFor(
        userId: UserId,
        today: LocalDate,
    ): List<Notice> = goalRepository
        .findAllByUserId(userId)
        .mapNotNull { goal -> noticeOrNull(goal, today) }

    private fun noticeOrNull(
        goal: Goal,
        today: LocalDate,
    ): Notice? {
        /*
         * 저장된 진행도를 그대로 믿지 않는다. 주기가 넘어갔는데 아직 아무도 기록하지 않았으면
         * DB 에는 지난 주기의 값이 남아 있다.
         *
         * `rollOverIfNeeded` 가 아니라 `progressOn` 을 쓴다 — 알림은 쓰기 트랜잭션 안에서
         * 도는데, 저쪽을 부르면 더티 체킹이 리셋을 저장해 버린다.
         */
        if (goal.isAchievedOn(today)) return null

        val endOfPeriod = goal.period.endOf(today)
        val daysLeft = endOfPeriod.toEpochDay() - today.toEpochDay()
        if (daysLeft > NOTICE_DAYS) return null

        val remaining = goal.target - goal.progressOn(today)
        return Notice(
            moduleId = GoalModule.MODULE_ID,
            on = endOfPeriod,
            title = goal.title,
            // 단위를 안 적었으면 지어내지 않는다 — "회"를 붙이면 "2권"이 맞는 목표에 "2회"가 나간다.
            // 화면도 같은 자리를 비운다(`progressLabel`).
            detail = "${label(daysLeft)} · ${remaining}${goal.unit ?: ""} 남았어요",
        )
    }

    private fun label(daysLeft: Long): String = if (daysLeft == 0L) "오늘까지" else "내일까지"
}
