package io.aetera.gateway.guide

import io.aetera.gateway.PostgresContainerConfig
import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideJourney
import io.aetera.model.guide.GuideJourneyId
import io.aetera.model.guide.GuideJourneyRepository
import io.aetera.model.guide.GuideTaskKey
import io.aetera.model.guide.GuideTaskProgress
import io.aetera.model.guide.GuideTaskProgressId
import io.aetera.model.guide.GuideTaskProgressRepository
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
import java.time.LocalDate

@Tag("integration")
@SpringBootTest
@Import(PostgresContainerConfig::class)
@Transactional
class GuideRepositoryJpaAdapterTest {
    @Autowired
    private lateinit var guideJourneyRepository: GuideJourneyRepository

    @Autowired
    private lateinit var guideTaskProgressRepository: GuideTaskProgressRepository

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    private val now: Instant = Instant.parse("2026-08-14T00:00:00Z")
    private val today: LocalDate = LocalDate.of(2026, 8, 14)
    private val guideId = GuideId("resignation")

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    private fun startJourney(
        owner: UserId,
        anchorDate: LocalDate = LocalDate.of(2026, 9, 30),
    ): GuideJourney {
        val saved =
            guideJourneyRepository.save(
                GuideJourney.start(GuideJourneyId.next(), owner, guideId, anchorDate, today, now),
            )
        flushAndClear()
        return saved
    }

    private fun saveProgress(
        journey: GuideJourney,
        taskKey: String,
        done: Boolean = true,
        note: String? = null,
    ) {
        guideTaskProgressRepository.save(
            GuideTaskProgress.create(
                id = GuideTaskProgressId.next(),
                journeyId = journey.id,
                taskKey = GuideTaskKey(taskKey),
                done = done,
                note = note,
                at = now,
            ),
        )
        flushAndClear()
    }

    @Test
    fun `여정을 저장하고 사용자·가이드로 다시 찾는다`() {
        val owner = UserId.next()
        startJourney(owner)

        val found = guideJourneyRepository.getByUserIdAndGuideId(owner, guideId)

        assertThat(found).isNotNull
        assertThat(found!!.anchorDate).isEqualTo(LocalDate.of(2026, 9, 30))
        assertThat(found.startedAt).isEqualTo(now)
    }

    @Test
    fun `다른 사용자의 여정은 보이지 않는다`() {
        startJourney(UserId.next())

        assertThat(guideJourneyRepository.getByUserIdAndGuideId(UserId.next(), guideId)).isNull()
    }

    @Test
    fun `기준일 변경은 INSERT 가 아니라 UPDATE 로 나간다`() {
        val owner = UserId.next()
        val journey = startJourney(owner)

        val reloaded = guideJourneyRepository.getByUserIdAndGuideId(owner, guideId)!!
        reloaded.changeAnchorDate(LocalDate.of(2026, 10, 31), today)
        guideJourneyRepository.save(reloaded)
        flushAndClear()

        val found = guideJourneyRepository.getByUserIdAndGuideId(owner, guideId)!!
        assertThat(found.id).isEqualTo(journey.id)
        assertThat(found.anchorDate).isEqualTo(LocalDate.of(2026, 10, 31))
        assertThat(entityManager.createQuery("select count(j) from GuideJourneyJpaEntity j", Long::class.java).singleResult)
            .isEqualTo(1L)
    }

    @Test
    fun `진행 상태를 여정 단위로 한 번에 읽는다`() {
        val journey = startJourney(UserId.next())
        saveProgress(journey, "severance-pay", done = true, note = "인사팀 확인함")
        saveProgress(journey, "final-payroll", done = false, note = "명세서 대조 예정")

        val progresses = guideTaskProgressRepository.findAllByJourneyId(journey.id)

        assertThat(progresses).hasSize(2)
        assertThat(progresses.single { it.taskKey.value == "severance-pay" }.note).isEqualTo("인사팀 확인함")
    }

    @Test
    fun `할 일 키로 단건을 찾는다`() {
        val journey = startJourney(UserId.next())
        saveProgress(journey, "severance-pay")

        assertThat(guideTaskProgressRepository.getByJourneyIdAndTaskKey(journey.id, GuideTaskKey("severance-pay")))
            .isNotNull
        assertThat(guideTaskProgressRepository.getByJourneyIdAndTaskKey(journey.id, GuideTaskKey("career-cert")))
            .isNull()
    }

    @Test
    fun `여정 초기화는 진행 상태를 먼저 지워 참조를 남기지 않는다`() {
        val owner = UserId.next()
        val journey = startJourney(owner)
        saveProgress(journey, "severance-pay")
        saveProgress(journey, "final-payroll")

        guideTaskProgressRepository.deleteAllByJourneyId(journey.id)
        guideJourneyRepository.delete(guideJourneyRepository.getByUserIdAndGuideId(owner, guideId)!!)
        flushAndClear()

        assertThat(guideTaskProgressRepository.findAllByJourneyId(journey.id)).isEmpty()
        assertThat(guideJourneyRepository.getByUserIdAndGuideId(owner, guideId)).isNull()
    }

    @Test
    fun `다른 여정의 진행 상태는 함께 지워지지 않는다`() {
        val mine = startJourney(UserId.next())
        val others = startJourney(UserId.next())
        saveProgress(mine, "severance-pay")
        saveProgress(others, "severance-pay")

        guideTaskProgressRepository.deleteAllByJourneyId(mine.id)
        flushAndClear()

        assertThat(guideTaskProgressRepository.findAllByJourneyId(mine.id)).isEmpty()
        assertThat(guideTaskProgressRepository.findAllByJourneyId(others.id)).hasSize(1)
    }
}
