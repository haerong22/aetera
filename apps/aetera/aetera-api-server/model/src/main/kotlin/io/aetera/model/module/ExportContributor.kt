package io.aetera.model.module

import io.aetera.model.user.UserId

/**
 * 내보내기에 자기 데이터를 내놓는 모듈.
 *
 * [TimelineContributor]·[NotificationContributor] 와 같은 방식으로 발견되고,
 * 같은 이유로 `model.module` 에 산다 — 다른 패키지에 두면 모듈 격리 규칙이 그 패키지를
 * 통째로 막아 모듈들이 이 인터페이스를 구현할 수 없다.
 *
 * **저 둘과 달리 [moduleIds] 가 없다.** 타임라인과 알림은 "켠 모듈이 하는 일"이라 꺼진
 * 모듈을 부르지 않지만, 내보내기는 **내 데이터를 돌려받는 일**이다. 모듈을 껐다고
 * 그동안 적은 것이 사라지지는 않으니(중지해도 데이터는 남는다고 스토어가 약속한다),
 * 꺼진 모듈의 데이터도 함께 내보낸다.
 */
interface ExportContributor {
    /**
     * 내보내기 파일에서 이 데이터가 놓일 자리. `"asset"`, `"income"` 처럼 모듈 아이디를 쓴다.
     *
     * 가이드처럼 한 기여자가 여럿을 대신하는 경우가 있어 아이디를 따로 받는다 —
     * 그때는 한 덩어리로 묶어 낸다.
     */
    val section: String

    /**
     * 이 사용자의 데이터 전부. 없으면 빈 목록.
     *
     * 줄 하나가 맵 하나다. **내보내기는 사람이 읽고 다른 곳에 옮길 수 있어야 하므로**
     * 내부 식별자(UUID)나 저장 구조를 그대로 흘리지 않고, 뜻이 있는 값만 담는다.
     */
    fun exportFor(userId: UserId): List<Map<String, Any?>>
}
