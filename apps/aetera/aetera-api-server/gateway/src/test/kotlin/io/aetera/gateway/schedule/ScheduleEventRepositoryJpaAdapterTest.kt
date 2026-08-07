package io.aetera.gateway.schedule

import io.aetera.gateway.PostgresContainerConfig
import io.aetera.model.schedule.ScheduleEvent
import io.aetera.model.schedule.ScheduleEventId
import io.aetera.model.schedule.ScheduleEventRepository
import io.aetera.model.schedule.SchedulePeriod
import io.aetera.model.user.UserId
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Tag("integration")
@SpringBootTest
@Import(PostgresContainerConfig::class)
@Transactional
class ScheduleEventRepositoryJpaAdapterTest {
    @Autowired
    private lateinit var scheduleEventRepository: ScheduleEventRepository

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    private val now: Instant = Instant.parse("2026-03-01T00:00:00Z")

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    private fun persistEvent(
        owner: UserId,
        startsAt: Instant,
        endsAt: Instant = startsAt.plusSeconds(3600),
        title: String = "일정",
    ): ScheduleEventId {
        val event =
            ScheduleEvent.create(
                id = ScheduleEventId.next(),
                userId = owner,
                title = title,
                description = "설명",
                startsAt = startsAt,
                endsAt = endsAt,
                allDay = false,
                color = "#3182f6",
                createdAt = now,
            )
        scheduleEventRepository.save(event)
        return event.id
    }

    @Test
    fun `일정을 왕복 저장한다`() {
        val owner = UserId.next()
        val id = persistEvent(owner, startsAt = now, title = "팀 회의")
        flushAndClear()

        val found = scheduleEventRepository.getById(id)

        assertThat(found).isNotNull
        requireNotNull(found)
        assertThat(found.userId).isEqualTo(owner)
        assertThat(found.title).isEqualTo("팀 회의")
        assertThat(found.color).isEqualTo("#3182f6")
    }

    @Test
    fun `기간과 겹치는 일정만 시작 시각 순으로 돌려준다`() {
        val owner = UserId.next()
        val someoneElse = UserId.next()

        val day1 = now.plusSeconds(86_400)
        val day2 = now.plusSeconds(2 * 86_400)
        val day9 = now.plusSeconds(9 * 86_400)

        persistEvent(owner, startsAt = day2, title = "둘째 날")
        persistEvent(owner, startsAt = day1, title = "첫째 날")
        persistEvent(owner, startsAt = day9, title = "범위 밖")
        persistEvent(someoneElse, startsAt = day1, title = "남의 일정")
        // 조회 범위를 가로지르는 장기 일정도 잡혀야 한다.
        persistEvent(owner, startsAt = now.minusSeconds(86_400), endsAt = day9.plusSeconds(86_400), title = "장기 일정")
        flushAndClear()

        val found =
            scheduleEventRepository.findAllOverlapping(
                userId = owner,
                period = SchedulePeriod(from = now, to = now.plusSeconds(5 * 86_400)),
            )

        assertThat(found.map { it.title }).containsExactly("장기 일정", "첫째 날", "둘째 날")
    }

    /**
     * 겹침 판정은 양 끝을 포함한다. 이 경계를 안 재면 조회 구간을 열린 구간으로 바꿔도
     * 기존 테스트가 전부 통과해서, 경계에 걸친 일정이 캘린더에서 조용히 사라진다.
     */
    @Test
    fun `조회 구간의 양 끝에 걸친 일정도 포함한다`() {
        val owner = UserId.next()
        val from = now
        val to = now.plusSeconds(5 * 86_400)

        // 구간 시작에 끝이 정확히 닿는 일정, 구간 끝에 시작이 정확히 닿는 일정
        persistEvent(owner, startsAt = from.minusSeconds(3600), endsAt = from, title = "왼쪽 경계")
        persistEvent(owner, startsAt = to, endsAt = to.plusSeconds(3600), title = "오른쪽 경계")
        // 1초 차이로 벗어난 일정들
        persistEvent(owner, startsAt = from.minusSeconds(7200), endsAt = from.minusSeconds(1), title = "왼쪽 밖")
        persistEvent(owner, startsAt = to.plusSeconds(1), endsAt = to.plusSeconds(3600), title = "오른쪽 밖")
        flushAndClear()

        val found = scheduleEventRepository.findAllOverlapping(owner, SchedulePeriod(from, to))

        assertThat(found.map { it.title }).containsExactlyInAnyOrder("왼쪽 경계", "오른쪽 경계")
    }

    @Test
    fun `수정한 내용이 저장된다`() {
        val owner = UserId.next()
        val id = persistEvent(owner, startsAt = now)
        flushAndClear()

        val event = requireNotNull(scheduleEventRepository.getById(id))
        event.update(
            title = "바뀐 제목",
            description = null,
            startsAt = now.plusSeconds(3600),
            endsAt = now.plusSeconds(7200),
            allDay = true,
            color = null,
        )
        scheduleEventRepository.save(event)
        flushAndClear()

        val reloaded = requireNotNull(scheduleEventRepository.getById(id))
        assertThat(reloaded.title).isEqualTo("바뀐 제목")
        assertThat(reloaded.allDay).isTrue()
        assertThat(reloaded.description).isNull()
    }

    @Test
    fun `삭제하면 조회되지 않는다`() {
        val owner = UserId.next()
        val id = persistEvent(owner, startsAt = now)
        flushAndClear()

        val event = requireNotNull(scheduleEventRepository.getById(id))
        scheduleEventRepository.delete(event)
        flushAndClear()

        assertThat(scheduleEventRepository.getById(id)).isNull()
    }
}
