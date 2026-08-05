package io.aetera.model.module

/**
 * 모듈이 스스로를 소개하는 메타데이터. 모듈 스토어 화면과 활성화 검증이 이걸 쓴다.
 */
data class ModuleDescriptor(
    val id: ModuleId,
    val displayName: String,
    val description: String,
    val category: ModuleCategory,
    val version: String = "0.1.0",
)
