package io.aetera.model.asset

/**
 * 자산의 갈래. [liability] 가 참이면 순자산에서 빼는 쪽이다.
 *
 * 부채도 금액은 양수로 적는다 — 사용자는 "전세대출 7,580만원"이라고 알고 있지
 * "마이너스 7,580만원"이라고 알고 있지 않다. 부호는 분류가 정한다.
 */
enum class AssetCategory(
    val liability: Boolean,
) {
    CASH(false),
    INVESTMENT(false),
    REAL_ESTATE(false),
    PENSION(false),
    DEBT(true),
    ETC(false),
}
