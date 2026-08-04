package io.aetera.shared.error

class CoreException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.defaultMessage,
) : RuntimeException(message) {
    val kind: ErrorKind get() = errorCode.kind
}

fun ensure(
    condition: Boolean,
    errorCode: ErrorCode,
    message: String = errorCode.defaultMessage,
) {
    if (!condition) throw CoreException(errorCode, message)
}
