package io.aetera.usecase.common

import io.aetera.model.common.UserOwned
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.aetera.shared.error.ErrorCode

/**
 * 남의 데이터는 존재 여부조차 알려주지 않는다 — 없는 것과 남의 것을 같은 코드로 거절한다.
 *
 * 둘을 구분해 응답하면 아이디를 바꿔 가며 호출하는 것만으로 남의 데이터가 있는지 알아낼 수 있다.
 * 모듈마다 이 판단을 다시 적으면 한 곳에서 실수했을 때 거기만 조용히 새므로 한곳에 둔다.
 */
internal fun <T : UserOwned> T?.orNotFound(
    owner: UserId,
    errorCode: ErrorCode,
    id: Any,
): T {
    if (this == null || userId != owner) {
        throw CoreException(errorCode, "${errorCode.defaultMessage} id=$id")
    }
    return this
}
