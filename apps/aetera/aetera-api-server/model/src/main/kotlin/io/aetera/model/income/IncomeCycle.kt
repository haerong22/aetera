package io.aetera.model.income

/**
 * 들어오는 주기. 고정지출과 같은 눈금이지만 **같은 enum 을 쓰지 않는다** —
 * 모듈끼리 서로를 참조하지 않는 것이 이 플랫폼의 규약이고, 한쪽이 눈금을 바꿀 때
 * 다른 쪽이 따라 흔들리면 안 된다.
 *
 * "없음"이 없다. 한 번 받고 끝나는 돈은 여기서 다룰 것이 아니다.
 */
enum class IncomeCycle(
    val months: Int,
) {
    MONTHLY(1),
    QUARTERLY(3),
    HALF_YEARLY(6),
    YEARLY(12),
}
