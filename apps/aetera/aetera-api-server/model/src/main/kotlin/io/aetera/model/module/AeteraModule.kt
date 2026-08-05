package io.aetera.model.module

/**
 * Aetera 모듈 SPI — 이 플랫폼에서 가장 중요한 계약.
 *
 * 새 모듈(가계부, 퇴사 가이드, ...)은 이 인터페이스를 구현한 빈을 usecase 계층에
 * 하나 등록하는 것으로 플랫폼에 편입된다. 코어(모듈 레지스트리)는 `List<AeteraModule>` 를
 * 주입받아 발견하므로, **모듈을 추가할 때 코어 코드는 한 줄도 바뀌지 않는다.**
 *
 * 모듈이 지켜야 하는 규약:
 * - REST API 는 전부 `/api/v1/modules/{module-id}/..` 아래에 둔다.
 *   활성화하지 않은 사용자의 접근은 코어 인터셉터가 일괄 차단하므로 모듈은 검사 코드를 갖지 않는다.
 * - 다른 모듈의 클래스를 직접 참조하지 않는다.
 * - 데이터에는 `userId` 를 평범한 값으로만 보관하고 사용자 테이블에 FK 를 걸지 않는다.
 */
interface AeteraModule {
    val descriptor: ModuleDescriptor
}
