package io.aetera.usecase.guide

import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideJourney
import io.aetera.model.guide.GuideJourneyId
import io.aetera.model.guide.GuideJourneyRepository
import io.aetera.model.guide.GuideTaskKey
import io.aetera.model.guide.GuideTaskProgress
import io.aetera.model.guide.GuideTaskProgressId
import io.aetera.model.guide.GuideTaskProgressRepository
import io.aetera.model.user.UserId
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.time.LocalDate

/**
 * 가이드는 기여자 하나가 네 모듈을 대신한다. 다른 기여자들과 달리 여정마다 진행을
 * 따로 읽어 엮으므로 여기만 시험이 필요하다.
 */
class GuideExportContributorTest :
    DescribeSpec({
        val journeyRepository = mockk<GuideJourneyRepository>()
        val progressRepository = mockk<GuideTaskProgressRepository>()

        // 콘텐츠는 진짜를 쓴다 — 가짜 템플릿이면 실제 제목이 바뀌어도 통과한다.
        val catalog = GuideCatalog(listOf(ResignationModule()))
        val sut = GuideExportContributor(journeyRepository, progressRepository, catalog)

        val owner = UserId.next()
        val journeyId = GuideJourneyId.next()
        val anchor = LocalDate.of(2026, 11, 30)
        val checkedAt = Instant.parse("2026-09-01T00:00:00Z")

        fun journey(guideId: String = "resignation") = GuideJourney.reconstitute(
            id = journeyId,
            userId = owner,
            guideId = GuideId(guideId),
            anchorDate = anchor,
            startedAt = Instant.parse("2026-08-01T00:00:00Z"),
        )

        fun progress(
            key: String,
            done: Boolean = true,
            note: String? = null,
        ) = GuideTaskProgress.reconstitute(
            id = GuideTaskProgressId.next(),
            journeyId = journeyId,
            taskKey = GuideTaskKey(key),
            done = done,
            note = note,
            updatedAt = checkedAt,
        )

        @Suppress("UNCHECKED_CAST")
        fun doneTasksOf(row: Map<String, Any?>) = row["doneTasks"] as List<Map<String, Any?>>

        describe("여정") {
            it("기준일과 시작 시각을 담는다") {
                every { journeyRepository.findAllByUserId(owner) } returns listOf(journey())
                every { progressRepository.findAllByJourneyId(journeyId) } returns emptyList()

                val row = sut.exportFor(owner).single()

                row["guideId"] shouldBe "resignation"
                row["title"] shouldBe "퇴사 준비"
                row["anchorDate"] shouldBe "2026-11-30"
            }

            it("시작한 가이드가 없으면 빈 목록") {
                every { journeyRepository.findAllByUserId(owner) } returns emptyList()

                sut.exportFor(owner).shouldBeEmpty()
            }

            // 콘텐츠에서 사라진 가이드의 낡은 여정이 남아 있을 수 있다.
            it("모르는 가이드면 제목만 비우고 나머지는 남긴다") {
                every { journeyRepository.findAllByUserId(owner) } returns listOf(journey("gone"))
                every { progressRepository.findAllByJourneyId(journeyId) } returns emptyList()

                val row = sut.exportFor(owner).single()

                row["guideId"] shouldBe "gone"
                row["title"] shouldBe null
            }
        }

        describe("체크한 할 일") {
            /*
             * 안 한 일은 콘텐츠를 보면 알 수 있다. 27개짜리 가이드 넷을 통째로 실으면
             * 내 기록보다 남의 양식이 더 많은 파일이 된다.
             */
            it("체크한 것만 담는다") {
                every { journeyRepository.findAllByUserId(owner) } returns listOf(journey())
                every { progressRepository.findAllByJourneyId(journeyId) } returns
                    listOf(
                        progress("finance-runway", done = true),
                        progress("company-rules", done = false),
                    )

                val tasks = doneTasksOf(sut.exportFor(owner).single())

                tasks shouldHaveSize 1
                tasks.single()["taskKey"] shouldBe "finance-runway"
            }

            it("제목과 메모를 함께 담는다") {
                every { journeyRepository.findAllByUserId(owner) } returns listOf(journey())
                every { progressRepository.findAllByJourneyId(journeyId) } returns
                    listOf(progress("company-rules", note = "인사팀 확인함"))

                val task = doneTasksOf(sut.exportFor(owner).single()).single()

                task["title"] shouldBe "취업규칙에서 퇴직 관련 조항 확인하기"
                task["note"] shouldBe "인사팀 확인함"
                task["checkedAt"] shouldBe checkedAt.toString()
            }

            // 콘텐츠가 바뀌어 할 일이 사라져도 내가 체크했다는 사실은 남아야 한다.
            it("모르는 할 일이면 키는 남기고 제목만 비운다") {
                every { journeyRepository.findAllByUserId(owner) } returns listOf(journey())
                every { progressRepository.findAllByJourneyId(journeyId) } returns listOf(progress("gone-task"))

                val task = doneTasksOf(sut.exportFor(owner).single()).single()

                task["taskKey"] shouldBe "gone-task"
                task["title"] shouldBe null
            }
        }
    })
