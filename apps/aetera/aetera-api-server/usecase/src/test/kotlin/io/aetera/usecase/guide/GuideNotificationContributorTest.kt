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
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.time.LocalDate
import io.kotest.matchers.collections.shouldContain as shouldContainItem

/**
 * 무엇을 **안** 보내는지가 이 기여자의 값어치다.
 *
 * 27개짜리 가이드를 그대로 흘리면 메일이 할 일 목록이 되고, 그러면 열어 보지 않게 되어
 * 정작 놓친 것도 함께 묻힌다.
 */
class GuideNotificationContributorTest :
    DescribeSpec({
        val journeyRepository = mockk<GuideJourneyRepository>()
        val progressRepository = mockk<GuideTaskProgressRepository>()

        // 콘텐츠는 진짜를 쓴다 — 가짜 템플릿으로 검증하면 실제 마감일이 바뀌어도 통과한다.
        val catalog = GuideCatalog(listOf(ResignationModule()))
        val sut = GuideNotificationContributor(journeyRepository, progressRepository, catalog)

        val owner = UserId.next()
        val journeyId = GuideJourneyId.next()
        val anchor = LocalDate.of(2026, 10, 31)

        /** 퇴사 준비의 필수 항목, 마감이 이른 순. */
        val runway = anchor.minusDays(60) // finance-runway
        val rules = anchor.minusDays(50) // company-rules
        val leave = anchor.minusDays(45) // annual-leave-check

        /** 사이에 낀 선택 항목. 아무리 지나도 나오면 안 된다. */
        val nextPlan = anchor.minusDays(55) // next-plan

        fun checked(vararg keys: String) {
            every { progressRepository.findAllByJourneyId(journeyId) } returns
                keys.map { key ->
                    GuideTaskProgress.reconstitute(
                        id = GuideTaskProgressId.next(),
                        journeyId = journeyId,
                        taskKey = GuideTaskKey(key),
                        done = true,
                        note = null,
                        updatedAt = Instant.parse("2026-09-01T00:00:00Z"),
                    )
                }
        }

        beforeEach {
            every { journeyRepository.findAllByUserId(owner) } returns
                listOf(
                    GuideJourney.reconstitute(
                        id = journeyId,
                        userId = owner,
                        guideId = GuideId("resignation"),
                        anchorDate = anchor,
                        startedAt = Instant.parse("2026-08-01T00:00:00Z"),
                    ),
                )
            checked()
        }

        describe("마감이 지난 것만") {
            it("아직 안 지났으면 조용하다") {
                sut.noticesFor(owner, runway.minusDays(1)).shouldBeEmpty()
            }

            it("마감 당일에 처음 알린다") {
                val notices = sut.noticesFor(owner, runway)

                notices shouldHaveSize 1
                notices.first().detail shouldContain "오늘까지예요"
            }

            it("지난 날수를 함께 적는다") {
                sut.noticesFor(owner, runway.plusDays(3)).first().detail shouldContain "3일 지났어요"
            }

            it("어느 가이드인지 밝힌다 — 둘 이상 켜면 제목만으로는 모른다") {
                sut.noticesFor(owner, runway).first().detail shouldContain "퇴사 준비"
            }
        }

        describe("오래 밀린 것은 뺀다") {
            it("30일까지는 계속 재촉한다") {
                sut.noticesFor(owner, runway.plusDays(30)).map { it.on } shouldContainItem runway
            }

            // 체크해야만 사라지는 줄이라, 그냥 두면 같은 잔소리가 영원히 간다.
            it("31일이 넘으면 접은 것으로 본다") {
                sut.noticesFor(owner, runway.plusDays(31)).map { it.on } shouldNotContain runway
            }
        }

        describe("세 줄까지") {
            /*
             * D-25 시점이면 살아 있는 후보가 여섯이다(D-50·-45·-40·-30·-28·-25).
             * D-60 은 25일이 아니라 55일 밀려 이미 접힌 뒤다.
             *
             * 후보가 상한보다 많은 날을 골라야 한다 — 처음엔 후보가 딱 셋인 날로 적었더니
             * 상한을 10으로 바꿔도 통과했다.
             */
            it("밀린 게 많아도 셋만, 오래된 것부터") {
                val notices = sut.noticesFor(owner, anchor.minusDays(25))

                notices shouldHaveSize 3
                notices.map { it.on } shouldBe listOf(rules, leave, anchor.minusDays(40))
            }
        }

        describe("체크한 것은 빠진다") {
            it("맨 앞을 체크하면 다음 것이 올라온다") {
                checked("finance-runway")

                sut.noticesFor(owner, leave.plusDays(1)).map { it.on } shouldNotContain runway
            }
        }

        describe("필수만") {
            // "다음 계획 구체화하기"(D-55, 선택)는 안 해도 되는 일이라 재촉하지 않는다.
            it("선택 항목은 마감이 지나도 나오지 않는다") {
                sut.noticesFor(owner, nextPlan.plusDays(1)).map { it.on } shouldNotContain nextPlan
            }
        }

        describe("여정이 없으면") {
            it("아무것도 내지 않는다") {
                every { journeyRepository.findAllByUserId(owner) } returns emptyList()

                sut.noticesFor(owner, anchor).shouldBeEmpty()
            }
        }
    })
