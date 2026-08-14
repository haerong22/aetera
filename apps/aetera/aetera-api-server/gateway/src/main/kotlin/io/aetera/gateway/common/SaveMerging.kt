package io.aetera.gateway.common

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

/**
 * 도메인 모델 하나를 저장한다 — 같은 아이디의 행이 있으면 그 행을 고치고, 없으면 새로 만든다.
 *
 * 어댑터는 저장할 때마다 모델에서 엔티티를 새로 빚는다. 그걸 그냥 `save` 하면 안 된다:
 * [UuidJpaEntity] 는 조회나 삽입을 거치지 않은 객체를 "새 것"으로 보므로, 이미 있는 행의
 * 아이디로 빚은 엔티티는 UPDATE 가 아니라 INSERT 로 나가 중복 키로 터진다.
 * 그래서 먼저 찾아보고, 찾았으면 **관리 중인 그 엔티티에** 값을 옮겨 담는다.
 *
 * 이 절차를 어댑터마다 손으로 적으면 두 가지 사고가 난다. [create] 만 쓰면 위의 중복 키로
 * 요란하게 터지지만, [update] 를 빠뜨리면 **수정이 조용히 사라진다** — 컴파일도 테스트도
 * 통과하고 아무 로그도 남지 않는다. 뒤쪽이 훨씬 위험해서 절차를 한곳에 모아 둔다.
 */
internal fun <E : UuidJpaEntity> JpaRepository<E, UUID>.saveMerging(
    id: UUID,
    update: (E) -> Unit,
    create: () -> E,
): E = save(findByIdOrNull(id)?.also(update) ?: create())
