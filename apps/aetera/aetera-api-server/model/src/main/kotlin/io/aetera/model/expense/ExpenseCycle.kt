package io.aetera.model.expense

/**
 * 결제 주기. 만기 관리와 달리 "없음"이 없다 — 한 번 내고 끝나는 돈은 고정지출이 아니다.
 */
enum class ExpenseCycle(
    val months: Int,
) {
    MONTHLY(1),
    QUARTERLY(3),
    HALF_YEARLY(6),
    YEARLY(12),
}
