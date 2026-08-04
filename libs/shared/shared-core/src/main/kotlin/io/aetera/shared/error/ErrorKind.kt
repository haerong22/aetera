package io.aetera.shared.error

/**
 * 전송 방식과 무관한 실패 분류.
 *
 * 도메인은 "무엇이 잘못됐는지"만 말하고, HTTP 상태 코드로 바꾸는 일은 인바운드 어댑터가 한다.
 * 덕분에 같은 유스케이스를 배치나 큐 리스너에서 불러도 의미가 유지된다.
 */
enum class ErrorKind {
    /** 호출자가 보낸 값을 도메인이 받아들일 수 없다. */
    INVALID_INPUT,

    /** 호출자가 누구인지 확인되지 않았다. */
    UNAUTHENTICATED,

    /** 호출자는 확인됐지만 권한이 없다. */
    FORBIDDEN,

    /** 참조한 대상이 없다. */
    NOT_FOUND,

    /** 요청 자체는 정상이나 현재 상태와 충돌한다. */
    CONFLICT,

    /** 호출자가 고칠 수 없는 실패. */
    INTERNAL,
}
