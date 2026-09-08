package io.aetera.model.module

import io.aetera.model.user.UserId
import java.time.LocalDate

/**
 * 타임라인에 실을 것을 내놓는 모듈.
 *
 * 코어는 `List<TimelineContributor>` 를 주입받아 발견하므로 **모듈이 늘어도 타임라인 코드는
 * 바뀌지 않는다** — [AeteraModule] 과 같은 방식이다.
 *
 * `model.module` 에 두는 이유가 있다. `model.timeline` 에 두면 모듈 격리 규칙이
 * `..timeline..` 을 통째로 막아 **일정·만기 모듈이 이 인터페이스를 구현할 수 없게 된다.**
 * 이건 특정 모듈의 것이 아니라 모듈이 코어와 만나는 자리라, 모듈 계약 옆이 제자리다.
 *
 * 값을 **계산해서 낼지 쌓아 뒀다가 낼지는 모듈이 정한다.** 일정·만기·자산은 이미 가진 것을
 * 기간으로 자르면 되고, 지나가면 사라지는 것(목표의 지난 주기 성적)은 그 모듈이 따로 남겨야 한다.
 * 코어는 둘을 구분하지 않는다.
 */
interface TimelineContributor {
    /**
     * 이 기여자가 대신 말하는 모듈들. **하나도 켜져 있지 않으면 부르지 않는다.**
     *
     * 대개 하나지만 여럿일 수 있다 — 가이드는 콘텐츠마다 모듈이지만 읽는 방식이 같아
     * 기여자 하나가 넷을 함께 낸다. 그래서 집합이다.
     */
    val moduleIds: Set<ModuleId>

    fun entriesIn(
        userId: UserId,
        from: LocalDate,
        to: LocalDate,
    ): List<TimelineEntry>
}

/**
 * 타임라인의 한 줄.
 *
 * **1년 뒤에도 기억할 만한 것만 낸다.** 오늘 점심 약속까지 실으면 한 달 뒤엔 스크롤해도
 * 아무것도 안 보인다 — 활동 기록이 아니라 이정표다.
 *
 * 줄마다 자기 모듈을 가리킨다. 기여자 하나가 여러 모듈을 낼 수 있어서(가이드) 코어가
 * 대신 찍어 줄 수 없고, 코어는 켜지지 않은 모듈의 줄을 여기서 한 번 더 걸러 낸다.
 */
data class TimelineEntry(
    val moduleId: ModuleId,
    /** 시각이 아니라 날짜다. 타임라인은 날짜 단위로 읽힌다. */
    val on: LocalDate,
    val title: String,
    val detail: String? = null,
)
