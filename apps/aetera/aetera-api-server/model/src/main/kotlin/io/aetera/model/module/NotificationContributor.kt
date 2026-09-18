package io.aetera.model.module

import io.aetera.model.user.UserId
import java.time.LocalDate

/**
 * "이건 알려야 한다"를 내놓는 모듈.
 *
 * [TimelineContributor] 와 같은 방식으로 발견되고, 같은 이유로 `model.module` 에 산다 —
 * `model.notification` 에 두면 모듈 격리 규칙이 `..notification..` 을 통째로 막아
 * 만기·가이드가 이 인터페이스를 구현할 수 없다.
 *
 * 둘이 묻는 것은 다르다. 타임라인은 **지나온 길**을, 여기는 **놓치면 안 되는 것**을 묻는다.
 * 만기일은 양쪽에 다 나오지만 뜻이 다르다 — 한쪽은 기록이고 한쪽은 재촉이다.
 */
interface NotificationContributor {
    /** 이 기여자가 대신 말하는 모듈들. 하나도 켜져 있지 않으면 부르지 않는다. */
    val moduleIds: Set<ModuleId>

    /**
     * [today] 기준으로 지금 알려야 할 것들. 없으면 빈 목록.
     *
     * 날짜를 받는 이유: 보내는 시점의 "오늘"은 **사용자의 시간대**에서 정해지는데,
     * 그 판단은 부르는 쪽이 이미 했다. 모듈이 서버 시계를 다시 읽으면 어긋난다.
     */
    fun noticesFor(
        userId: UserId,
        today: LocalDate,
    ): List<Notice>
}

/**
 * 알림 한 줄.
 *
 * **놓치면 손해인 것만 낸다.** 오늘 할 일까지 실으면 매일 같은 메일이 오고, 그러면
 * 열어 보지 않게 되어 정작 중요한 것도 함께 묻힌다.
 */
data class Notice(
    val moduleId: ModuleId,
    /** 이 일이 걸린 날. 메일은 가까운 것부터 늘어놓는다. */
    val on: LocalDate,
    val title: String,
    val detail: String? = null,
)
