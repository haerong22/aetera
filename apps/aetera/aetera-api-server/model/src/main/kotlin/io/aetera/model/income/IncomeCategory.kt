package io.aetera.model.income

/**
 * 소득의 갈래. [stopsWhenLeaving] 은 **일을 그만두면 그 돈이 멈추는가**를 말한다.
 *
 * 월급만 멈춘다고 본다. 부업·임대·금융·연금은 다니던 직장과 무관하게 이어지고,
 * 지원금(실업급여)은 오히려 그만둔 뒤에 나온다.
 *
 * 이 한 칸 때문에 "버틸 개월 수"가 말이 된다 — 퇴사하면 끊길 월급을 남는 소득으로 세면
 * 계산이 실제보다 한참 넉넉해진다.
 */
enum class IncomeCategory(
    val stopsWhenLeaving: Boolean,
) {
    SALARY(true),
    SIDE(false),
    RENTAL(false),
    FINANCIAL(false),
    PENSION(false),
    BENEFIT(false),
    ETC(false),
}
