package io.aetera.model.guide

/**
 * 가이드 안에서 할 일 하나를 가리키는 안정적인 식별자.
 *
 * 사용자의 체크 상태가 이 값으로 저장되므로 **한 번 배포한 키는 바꾸지 않는다** —
 * 키를 바꾸면 그 항목을 체크해 둔 사람들의 진행이 조용히 사라진다.
 * 콘텐츠(제목·설명·마감)는 얼마든지 고쳐도 되지만 키만은 고정이다.
 *
 * URL 경로 조각으로도 쓰이므로 점(`.`) 없이 소문자-대시만 허용한다.
 *
 * 형식 위반을 도메인 에러 코드가 아니라 [require] 로 막는 이유: 이 생성자에 닿는 값은
 * 콘텐츠 작성자가 적은 리터럴(기동 시점)이거나 DB 에서 되살린 값뿐이다. 신뢰할 수 없는 입력은
 * [parseOrNull] 이 받아서 "가이드에 없는 할 일"로 바꾸므로, 여기까지 온 잘못된 값은
 * 사용자 입력 오류(400)가 아니라 우리 쪽 결함이다. [GuideTemplate] 의 키 중복 검사도 같은 이유로 require 다.
 */
@JvmInline
value class GuideTaskKey(
    val value: String,
) {
    init {
        require(PATTERN.matches(value)) { "'$value'는 올바른 할 일 키가 아닙니다. 소문자로 시작하는 소문자-대시만 씁니다." }
    }

    override fun toString(): String = value

    companion object {
        private val PATTERN = Regex("^[a-z][a-z0-9-]{0,79}$")

        /** 신뢰할 수 없는 입력(URL 경로 조각)용. 형식이 아니면 예외 대신 null 을 준다. */
        fun parseOrNull(raw: String): GuideTaskKey? = raw.takeIf(PATTERN::matches)?.let(::GuideTaskKey)
    }
}
