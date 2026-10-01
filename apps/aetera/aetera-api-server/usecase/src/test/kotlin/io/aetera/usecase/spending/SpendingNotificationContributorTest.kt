package io.aetera.usecase.spending

import io.aetera.model.spending.SpendingRecord
import io.aetera.model.spending.SpendingRecordId
import io.aetera.model.spending.SpendingRecordRepository
import io.aetera.model.user.UserId
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.time.LocalDate

/**
 * 이 기여자가 막는 것은 **모듈이 조용히 쓸모없어지는 것**이다.
 *
 * 달에 한 번 적는 일은 잊기 쉽고, 잊으면 평균이 낡은 채로 런웨이에 흘러간다 —
 * 사용자는 틀린 줄도 모르고 그 숫자를 본다.
 *
 * 동시에 **잔소리가 되면 안 된다.** 안 적은 사람에게 한 달 내내 같은 줄이 가면
 * 다이제스트를 열지 않게 되고, 그러면 만기처럼 정말 놓치면 손해인 것까지 묻힌다.
 * 그래서 시험의 절반이 "언제 **안** 내는가"다.
 */
class SpendingNotificationContributorTest :
    DescribeSpec({
        val repository = mockk<SpendingRecordRepository>()
        val sut = SpendingNotificationContributor(repository)
        val owner = UserId.next()

        /** 9월 1일. 지난 달은 8월이다. */
        val august = LocalDate.of(2026, 8, 1)

        fun record(month: LocalDate) = SpendingRecord.reconstitute(
            id = SpendingRecordId.next(),
            userId = owner,
            month = month,
            amount = 480_000,
            note = null,
            recordedAt = Instant.parse("2026-09-01T00:00:00Z"),
        )

        beforeTest {
            clearMocks(repository)
            every { repository.findByUserIdAndMonth(owner, any()) } returns null
        }

        describe("알릴 때") {
            it("지난 달을 안 적었으면 알린다") {
                sut.noticesFor(owner, LocalDate.of(2026, 9, 1)) shouldHaveSize 1
            }

            it("어느 달인지 말해 준다") {
                val notice = sut.noticesFor(owner, LocalDate.of(2026, 9, 3)).single()

                notice.title shouldContain "8월"
            }

            // 무엇을 하면 되는지 적어 준다 — "안 적었어요"만으로는 무엇을 보고 적을지 모른다.
            it("무엇을 하면 되는지 적어 준다") {
                val notice = sut.noticesFor(owner, LocalDate.of(2026, 9, 1)).single()

                notice.detail shouldContain "명세서"
            }

            /*
             * 걸린 날을 오늘로 두면 다이제스트가 가까운 것부터 늘어놓을 때 만기와 섞여
             * 맨 위로 올라온다 — 재촉의 세기가 다르다. 지난 달의 마지막 날로 둔다.
             */
            it("걸린 날은 그 달의 마지막 날이다") {
                val notice = sut.noticesFor(owner, LocalDate.of(2026, 9, 1)).single()

                notice.on shouldBe LocalDate.of(2026, 8, 31)
            }

            it("창의 마지막 날까지는 알린다") {
                sut.noticesFor(owner, LocalDate.of(2026, 9, 7)) shouldHaveSize 1
            }
        }

        describe("알리지 않을 때") {
            it("이미 적었으면 알리지 않는다") {
                every { repository.findByUserIdAndMonth(owner, august) } returns record(august)

                sut.noticesFor(owner, LocalDate.of(2026, 9, 1)).shouldBeEmpty()
            }

            /*
             * 창을 닫지 않으면 안 적은 사람에게 **한 달 내내 같은 줄**이 간다.
             * 그러면 다이제스트를 열지 않게 되고 정말 중요한 것까지 함께 묻힌다.
             */
            it("창을 벗어나면 조용히 넘어간다") {
                sut.noticesFor(owner, LocalDate.of(2026, 9, 8)).shouldBeEmpty()
                sut.noticesFor(owner, LocalDate.of(2026, 9, 20)).shouldBeEmpty()
            }

            // 창 밖이면 저장소를 묻지도 않는다 — 매일 모든 사용자에게 도는 길이다.
            it("창 밖에서는 저장소를 읽지 않는다") {
                sut.noticesFor(owner, LocalDate.of(2026, 9, 20))

                verify(exactly = 0) { repository.findByUserIdAndMonth(any(), any()) }
            }
        }

        describe("해가 바뀔 때") {
            // 1월에 지난 달은 작년 12월이다. 달만 빼면 1월 → 0월이 된다.
            it("1월에는 작년 12월을 묻는다") {
                sut.noticesFor(owner, LocalDate.of(2026, 1, 2))

                verify { repository.findByUserIdAndMonth(owner, LocalDate.of(2025, 12, 1)) }
            }

            it("1월 알림은 12월 31일에 걸린다") {
                val notice = sut.noticesFor(owner, LocalDate.of(2026, 1, 2)).single()

                notice.on shouldBe LocalDate.of(2025, 12, 31)
            }
        }
    })
