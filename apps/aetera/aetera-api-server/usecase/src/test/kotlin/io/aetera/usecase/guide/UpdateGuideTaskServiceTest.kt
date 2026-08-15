package io.aetera.usecase.guide

import io.aetera.model.guide.GuideErrorCode
import io.aetera.model.guide.GuideJourney
import io.aetera.model.guide.GuideJourneyId
import io.aetera.model.guide.GuideJourneyRepository
import io.aetera.model.guide.GuideTaskKey
import io.aetera.model.guide.GuideTaskProgress
import io.aetera.model.guide.GuideTaskProgressId
import io.aetera.model.guide.GuideTaskProgressRepository
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.aetera.usecase.guide.cmd.UpdateGuideTaskCommand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class UpdateGuideTaskServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-08-14T00:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)

        val journeyRepository = mockk<GuideJourneyRepository>()
        val progressRepository = mockk<GuideTaskProgressRepository>()

        // 콘텐츠는 진짜를 쓴다 — 가짜 템플릿으로 검증하면 실제 키가 바뀌어도 테스트가 통과한다.
        val catalog = GuideCatalog(listOf(ResignationModule()))
        val findGuideService = FindGuideService(catalog, journeyRepository, progressRepository)
        val sut = UpdateGuideTaskService(catalog, journeyRepository, progressRepository, findGuideService, clock)

        val owner = UserId.next()
        val guideId = "resignation"
        val taskKey = GuideTaskKey("severance-pay")

        val journey =
            GuideJourney.start(
                id = GuideJourneyId.next(),
                userId = owner,
                guideId = RESIGNATION_GUIDE.id,
                anchorDate = LocalDate.of(2026, 9, 30),
                now = now,
            )

        fun command(
            key: String = taskKey.value,
            done: Boolean = true,
            note: String? = null,
        ) = UpdateGuideTaskCommand(
            userId = owner.value,
            guideId = guideId,
            taskKey = key,
            done = done,
            note = note,
        )

        fun progressOf(
            done: Boolean,
            note: String?,
        ) = GuideTaskProgress.create(
            id = GuideTaskProgressId.next(),
            journeyId = journey.id,
            taskKey = taskKey,
            done = done,
            note = note,
            at = now,
        )

        beforeTest {
            clearMocks(journeyRepository, progressRepository)
            every { journeyRepository.getByUserIdAndGuideId(owner, RESIGNATION_GUIDE.id) } returns journey
            every { progressRepository.save(any()) } answers { firstArg() }
            every { progressRepository.delete(any()) } returns Unit
            every { progressRepository.findAllByJourneyId(journey.id) } returns emptyList()
        }

        describe("체크 저장") {
            it("처음 체크하면 진행 행이 생기고 진행률에 반영된다") {
                every { progressRepository.getByJourneyIdAndTaskKey(journey.id, taskKey) } returns null
                every { progressRepository.findAllByJourneyId(journey.id) } returns listOf(progressOf(true, null))

                val view = sut.updateTask(command(done = true))

                verify(exactly = 1) { progressRepository.save(any()) }
                view.progress.done shouldBe 1
                view.progress.requiredDone shouldBe 1
                view.phases
                    .flatMap { it.tasks }
                    .single { it.key == taskKey.value }
                    .done shouldBe true
            }

            it("이미 있는 행은 새로 만들지 않고 갱신한다") {
                val existing = progressOf(done = false, note = null)
                every { progressRepository.getByJourneyIdAndTaskKey(journey.id, taskKey) } returns existing

                sut.updateTask(command(done = true, note = "인사팀 확인함"))

                existing.done shouldBe true
                existing.note shouldBe "인사팀 확인함"
                verify(exactly = 1) { progressRepository.save(existing) }
            }

            it("체크를 풀고 메모까지 비우면 행을 지운다 — 손대기 전 상태로 되돌린다") {
                val existing = progressOf(done = true, note = "메모")
                every { progressRepository.getByJourneyIdAndTaskKey(journey.id, taskKey) } returns existing

                sut.updateTask(command(done = false, note = null))

                verify(exactly = 1) { progressRepository.delete(existing) }
                verify(exactly = 0) { progressRepository.save(any()) }
            }

            it("손댄 적 없는 항목을 빈 상태로 보내면 아무 행도 만들지 않는다") {
                every { progressRepository.getByJourneyIdAndTaskKey(journey.id, taskKey) } returns null

                val view = sut.updateTask(command(done = false, note = null))

                verify(exactly = 0) { progressRepository.save(any()) }
                verify(exactly = 0) { progressRepository.delete(any()) }
                view.progress.done shouldBe 0
            }
        }

        describe("거절") {
            it("가이드에 없는 할 일이면 TASK_NOT_FOUND") {
                shouldThrow<CoreException> { sut.updateTask(command(key = "no-such-task")) }
                    .errorCode shouldBe GuideErrorCode.TASK_NOT_FOUND
            }

            it("키 형식이 틀려도 400 이 아니라 TASK_NOT_FOUND — 같은 상황에 두 응답이 나가지 않게 한다") {
                shouldThrow<CoreException> { sut.updateTask(command(key = "../../etc/passwd")) }
                    .errorCode shouldBe GuideErrorCode.TASK_NOT_FOUND
            }

            it("가이드가 아닌 모듈 아이디면 GUIDE_NOT_FOUND") {
                shouldThrow<CoreException> {
                    sut.updateTask(command().copy(guideId = "schedule"))
                }.errorCode shouldBe GuideErrorCode.GUIDE_NOT_FOUND
            }

            it("여정을 시작하지 않았으면 JOURNEY_NOT_STARTED") {
                every { journeyRepository.getByUserIdAndGuideId(owner, RESIGNATION_GUIDE.id) } returns null

                shouldThrow<CoreException> { sut.updateTask(command()) }
                    .errorCode shouldBe GuideErrorCode.JOURNEY_NOT_STARTED
            }
        }

        describe("조회") {
            it("여정을 시작하기 전에는 마감일이 비어 있고 진행률이 0 이다") {
                every { journeyRepository.getByUserIdAndGuideId(owner, RESIGNATION_GUIDE.id) } returns null

                val view = findGuideService.findGuide(owner.value, guideId)

                view.journey.shouldBeNull()
                view.progress.done shouldBe 0
                view.phases.flatMap { it.tasks }.forEach { it.dueDate.shouldBeNull() }
                verify(exactly = 0) { progressRepository.findAllByJourneyId(any()) }
            }

            it("여정을 시작하면 각 항목의 마감이 기준일 기준 실제 날짜로 채워진다") {
                val view = findGuideService.findGuide(owner.value, guideId)

                view.journey?.anchorDate shouldBe LocalDate.of(2026, 9, 30)
                view.phases
                    .flatMap { it.tasks }
                    .single { it.key == "severance-pay" }
                    .dueDate shouldBe LocalDate.of(2026, 10, 14)
            }
        }
    })
