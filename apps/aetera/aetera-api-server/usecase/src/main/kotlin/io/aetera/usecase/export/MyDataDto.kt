package io.aetera.usecase.export

/**
 * 내보내기 파일 한 벌.
 *
 * 모듈별 데이터를 [modules] 아래 섹션으로 나눠 담는다 — 한 배열에 섞어 두면 받는 쪽이
 * 무엇이 무엇인지 가려내야 한다.
 */
data class MyDataDto(
    /** 언제 받아 간 것인지. 여러 벌을 갖고 있을 때 최신을 가린다. */
    val exportedAt: String,
    val profile: Map<String, Any?>,
    val modules: Map<String, List<Map<String, Any?>>>,
)
