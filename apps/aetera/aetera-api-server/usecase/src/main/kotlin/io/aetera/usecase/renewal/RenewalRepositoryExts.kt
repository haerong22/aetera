package io.aetera.usecase.renewal

import io.aetera.model.renewal.Renewal
import io.aetera.model.renewal.RenewalErrorCode
import io.aetera.model.renewal.RenewalId
import io.aetera.model.renewal.RenewalRepository
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException

/** 남의 항목은 존재 여부조차 알려주지 않는다 — 없는 것과 남의 것을 같은 코드로 거절한다. */
internal fun RenewalRepository.getOwnedOrThrow(
    id: RenewalId,
    owner: UserId,
): Renewal {
    val renewal = getById(id)
    if (renewal == null || renewal.userId != owner) {
        throw CoreException(RenewalErrorCode.RENEWAL_NOT_FOUND, "만기 항목을 찾을 수 없습니다. id=$id")
    }
    return renewal
}
