package io.aetera.usecase.spending

import io.aetera.model.spending.SpendingRecord
import io.aetera.model.spending.SpendingRecordId
import io.aetera.model.spending.SpendingRecordRepository
import io.aetera.model.user.UserId
import io.aetera.usecase.spending.cmd.SaveSpendingCommand
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * 이 유스케이스가 지키는 것은 **한 달에 하나**다.
 *
 * 명세서를 다시 보고 고치는 일이 흔한데, 그때마다 줄이 쌓이면 합계와 평균이 전부 틀어진다.
 */
class SaveSpendingServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-09-15T00:00:00Z")
        val today = LocalDate.of(2026, 9, 15)
        val repository = mockk<SpendingRecordRepository>()
        val sut = SaveSpendingService(repository, Clock.fixed(now, ZoneOffset.UTC))

        val owner = UserId.next()
        val month = LocalDate.of(2026, 9, 1)

        fun command(
            amount: Long = 480_000,
            month: LocalDate = LocalDate.of(2026, 9, 1),
        ) = SaveSpendingCommand(owner.value, month, amount, null)

        beforeTest {
            clearMocks(repository)
            every { repository.save(any()) } answers { firstArg() }
        }

        describe("처음 적을 때") {
            it("새 기록을 만든다") {
                every { repository.findByUserIdAndMonth(owner, month) } returns null

                sut.save(command(), today).amount shouldBe 480_000
            }
        }

        describe("다시 적을 때") {
            val existingId = SpendingRecordId.next()
            val existing =
                SpendingRecord.reconstitute(
                    id = existingId,
                    userId = owner,
                    month = month,
                    amount = 300_000,
                    note = null,
                    recordedAt = now.minusSeconds(86_400),
                )

            it("덮어쓴다") {
                every { repository.findByUserIdAndMonth(owner, month) } returns existing

                sut.save(command(amount = 520_000), today).amount shouldBe 520_000
            }

            /*
             * 식별자를 그대로 써야 **고친 것**이 된다. 새로 만들면 지운 자리에 새 줄이
             * 들어가는 셈이라, 내보낸 데이터를 들고 비교하는 사람에게는 같은 달이
             * 두 번 바뀐 것처럼 보인다.
             */
            it("같은 식별자를 쓴다") {
                every { repository.findByUserIdAndMonth(owner, month) } returns existing
                val saved = slot<SpendingRecord>()
                every { repository.save(capture(saved)) } answers { firstArg() }

                sut.save(command(amount = 520_000), today)

                saved.captured.id shouldBe existingId
            }
        }

        describe("달 맞추기") {
            // 며칠을 보내도 같은 달이면 같은 기록을 찾아야 한다 — 아니면 덮어쓰기가 안 걸린다.
            it("말일을 보내도 그 달의 1일로 찾는다") {
                every { repository.findByUserIdAndMonth(owner, month) } returns null

                sut.save(command(month = LocalDate.of(2026, 9, 30)), today).month shouldBe month
            }
        }
    })
