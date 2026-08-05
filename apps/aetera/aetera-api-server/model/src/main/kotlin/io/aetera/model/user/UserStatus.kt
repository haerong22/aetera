package io.aetera.model.user

enum class UserStatus {
    ACTIVE,
    WITHDRAWN,
    ;

    val isTerminated: Boolean get() = this == WITHDRAWN
}
