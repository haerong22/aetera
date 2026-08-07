package io.aetera.gateway.common

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Transient
import jakarta.persistence.Version
import org.springframework.data.domain.Persistable
import java.util.UUID

/**
 * 애플리케이션이 직접 만든 UUID 를 기본키로 쓰는 엔티티의 공통 뼈대.
 *
 * 기본키를 우리가 미리 정하면 Spring Data 는 "이 객체가 새 것인지" 알 수 없어서 `merge` 로
 * 흘러가고, 그러면 UPDATE 마다 SELECT 가 한 번 더 나간다. [Persistable] 로 그 판단을 넘겨주되,
 * 판단 근거는 [persisted] 플래그가 갖는다 — INSERT 직후([PostPersist])와 조회 직후([PostLoad])에
 * 켜지므로, 그 두 경로를 거치지 않은 객체만 새 것으로 취급된다.
 *
 * 이 블록이 엔티티마다 복사돼 있으면 새 모듈이 엔티티를 추가할 때 [markPersisted] 를 빠뜨려도
 * 컴파일은 통과하고, UPDATE 가 INSERT 로 나가는 사고만 조용히 생긴다. 그래서 한곳에 둔다.
 */
@MappedSuperclass
abstract class UuidJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val uid: UUID,
) : Persistable<UUID> {
    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0
        protected set

    @Transient
    private var persisted: Boolean = false

    override fun getId(): UUID = uid

    override fun isNew(): Boolean = !persisted

    @PostPersist
    @PostLoad
    fun markPersisted() {
        persisted = true
    }
}
