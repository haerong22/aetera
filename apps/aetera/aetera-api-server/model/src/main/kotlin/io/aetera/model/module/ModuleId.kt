package io.aetera.model.module

import io.aetera.shared.error.CoreException

/**
 * 모듈의 정체성. API 경로(`/api/v1/modules/{module-id}/..`)와 프론트엔드 레지스트리가
 * 같은 값을 쓰므로 소문자-대시 형식으로 고정한다.
 */
@JvmInline
value class ModuleId(
    val value: String,
) {
    init {
        if (!PATTERN.matches(value)) {
            throw CoreException(ModuleErrorCode.INVALID_MODULE_ID, "'$value'는 올바른 모듈 아이디가 아닙니다.")
        }
    }

    override fun toString(): String = value

    companion object {
        private val PATTERN = Regex("^[a-z][a-z0-9-]{0,49}$")

        /**
         * 신뢰할 수 없는 입력(주로 URL 경로 조각)용. 형식이 아니면 예외 대신 null 을 준다 —
         * 부르는 쪽이 "모르는 모듈"로 처리할 수 있어야 예외를 흐름 제어로 쓰지 않는다.
         */
        fun parseOrNull(raw: String): ModuleId? = raw.takeIf(PATTERN::matches)?.let(::ModuleId)
    }
}
