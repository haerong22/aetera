package io.aetera.model.common

import io.aetera.model.user.UserId

/**
 * 사용자 한 명에게 속하는 모듈 데이터.
 *
 * [userId] 는 평범한 값이다 — 모듈 데이터는 사용자 테이블에 FK 를 걸지 않는다(플랫폼 규약).
 * 이 인터페이스는 "누구 것인가"를 묻는 코드가 모듈마다 같은 규칙을 쓰게 하려고 둔다.
 */
interface UserOwned {
    val userId: UserId
}
