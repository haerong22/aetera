package io.aetera.shared.error

/**
 * 도메인이 선언하는 에러 정체성.
 *
 * HTTP 상태 코드를 들고 있지 않다. `상세 응답 코드 = HTTP status + 일련번호` 조합은
 * [ErrorKind] 를 HTTP 로 번역할 수 있는 인바운드 어댑터가 만든다.
 *
 * 일련번호는 4자리(1~9999)이고 도메인마다 [BAND_SIZE] 폭의 대역을 하나씩 받는다.
 * 어느 도메인이 어느 대역을 쓰는지는 아래 companion 이 한 곳에서 관리한다.
 */
interface ErrorCode {
    val kind: ErrorKind
    val sequence: Int
    val defaultMessage: String

    companion object {
        /** 도메인 하나가 받는 대역의 폭. 일련번호가 4자리이므로 도메인은 99개까지 늘어난다. */
        const val BAND_SIZE: Int = 100

        /**
         * `0` 번 대역(`1~99`)은 도메인 몫이 아니다. 앞절반(`1~49`)은 프로토콜 에러
         * (controller 의 `WebErrorCode`) 가 쓰고, 뒷절반은 도메인을 가리지 않는 공통 에러 몫으로
         * 비워 둔다. 이 상수가 그 경계다.
         */
        const val COMMON_BAND: Int = 50

        // ── 도메인 대역 등록부 ──
        // 새 도메인은 여기에 안 쓰인 배수로 한 줄 추가한다. 상수를 먼저 등록하지 않으면
        // 도메인 쪽에서 참조할 심볼이 없어 컴파일이 안 된다.
        //
        // 한 번 쓴 번호는 회수하지 않는다. 폐기된 도메인의 번호를 재사용하면 이미 나간 응답과
        // 남아 있는 로그의 의미가 바뀐다.

        const val USER_BAND: Int = 1 * BAND_SIZE
        const val AUTH_BAND: Int = 2 * BAND_SIZE
        const val MODULE_BAND: Int = 3 * BAND_SIZE
        const val SCHEDULE_BAND: Int = 4 * BAND_SIZE
        const val GUIDE_BAND: Int = 5 * BAND_SIZE
        const val RENEWAL_BAND: Int = 6 * BAND_SIZE
        const val GOAL_BAND: Int = 7 * BAND_SIZE
        const val EXPENSE_BAND: Int = 8 * BAND_SIZE
    }
}
