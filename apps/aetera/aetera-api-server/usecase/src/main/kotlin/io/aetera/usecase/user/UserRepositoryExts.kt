package io.aetera.usecase.user

import io.aetera.model.user.User
import io.aetera.model.user.UserErrorCode
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.shared.error.CoreException

internal fun UserRepository.getByIdOrThrow(id: UserId): User =
    getById(id) ?: throw CoreException(UserErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다. id=$id")
