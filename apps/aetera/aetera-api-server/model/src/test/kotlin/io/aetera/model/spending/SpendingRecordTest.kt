package io.aetera.model.spending

import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.LocalDate

class SpendingRecordTest :
    DescribeSpec({
        val owner = UserId.next()
        val today = LocalDate.of(2026, 9, 15)
        val now = Instant.parse("2026-09-15T00:00:00Z")

        fun record(
            month: LocalDate,
            amount: Long = 480_000,
            note: String? = null,
        ) = SpendingRecord.create(
            id = SpendingRecordId.next(),
            userId = owner,
            month = month,
            amount = amount,
            note = note,
            today = today,
            recordedAt = now,
        )

        describe("달") {
            // 화면이 며칠을 보내든 같은 달이면 같은 기록이어야 한다 — 아니면 한 달에 줄이 여럿 생긴다.
            it("며칠을 보내도 그 달의 1일로 맞춘다") {
                record(LocalDate.of(2026, 8, 31)).month shouldBe LocalDate.of(2026, 8, 1)
            }

            it("이번 달까지 적을 수 있다") {
                record(LocalDate.of(2026, 9, 1)).month shouldBe LocalDate.of(2026, 9, 1)
            }

            /*
             * 아직 오지 않은 달은 쓴 돈이 있을 수 없다. 허용하면 평균에 미래의 0 이나
             * 짐작한 값이 섞여, 런웨이가 **지나 봐야 아는 돈**을 미리 아는 척하게 된다.
             */
            it("다음 달은 막는다") {
                shouldThrow<CoreException> { record(LocalDate.of(2026, 10, 1)) }
                    .errorCode shouldBe SpendingErrorCode.INVALID_MONTH
            }
        }

        describe("금액") {
            /*
             * 0 을 허용해야 "안 쓴 달"과 "안 적은 달"이 구분된다. 그 구분이 평균을 가른다.
             */
            it("0원을 허용한다") {
                record(LocalDate.of(2026, 9, 1), amount = 0).amount shouldBe 0
            }

            it("음수를 막는다") {
                shouldThrow<CoreException> { record(LocalDate.of(2026, 9, 1), amount = -1) }
                    .errorCode shouldBe SpendingErrorCode.INVALID_AMOUNT
            }
        }

        describe("메모") {
            it("빈 메모는 null 로 둔다") {
                record(LocalDate.of(2026, 9, 1), note = "   ").note.shouldBeNull()
            }

            it("너무 긴 메모를 막는다") {
                shouldThrow<CoreException> { record(LocalDate.of(2026, 9, 1), note = "가".repeat(201)) }
                    .errorCode shouldBe SpendingErrorCode.INVALID_NOTE
            }
        }

        describe("평균") {
            it("최근 달들의 평균을 낸다") {
                val records =
                    listOf(
                        record(LocalDate.of(2026, 9, 1), amount = 600_000),
                        record(LocalDate.of(2026, 8, 1), amount = 400_000),
                    )

                records.averageMonthly(today, 6) shouldBe MonthlyAverage(500_000, 2)
            }

            /*
             * **0원으로 적은 달은 세고, 안 적은 달은 세지 않는다.** 그 구분이 평균을 가른다.
             *
             * 위 시험이 "안 적은 달을 0 으로 세지 않는다"를 이미 보여 준다 — 여섯 달 창에
             * 기록이 둘뿐인데 평균이 500,000 이므로 나머지 넷은 세지 않았다(0 으로 셌다면
             * 166,666 이다). 여기서는 그 반대쪽, **적어 둔 0 은 제대로 세는지**를 본다.
             */
            it("0원이라 적은 달은 평균을 끌어내린다") {
                val records =
                    listOf(
                        record(LocalDate.of(2026, 9, 1), amount = 600_000),
                        record(LocalDate.of(2026, 8, 1), amount = 400_000),
                        record(LocalDate.of(2026, 7, 1), amount = 0),
                    )

                // 안 적은 달이었다면 500,000 이었을 것이다.
                records.averageMonthly(today, 6) shouldBe MonthlyAverage(333_333, 3)
            }

            it("창 밖의 달은 세지 않는다") {
                val records =
                    listOf(
                        record(LocalDate.of(2026, 9, 1), amount = 600_000),
                        // 여섯 달 창(4월~9월) 밖이다.
                        record(LocalDate.of(2026, 1, 1), amount = 100_000),
                    )

                records.averageMonthly(today, 6) shouldBe MonthlyAverage(600_000, 1)
            }

            it("창 안에 기록이 없으면 모른다고 답한다") {
                val records = listOf(record(LocalDate.of(2026, 1, 1)))

                records.averageMonthly(today, 6).shouldBeNull()
            }

            it("아무 기록도 없으면 모른다고 답한다") {
                emptyList<SpendingRecord>().averageMonthly(today, 6).shouldBeNull()
            }

            /*
             * 평균과 센 달 수를 **함께** 돌려주는 이유. 따로 세면 창을 두 곳에서 계산하게 되고,
             * 그러면 "6개월 평균"이라 적힌 값이 실제로는 세 달치일 수 있다 — 신뢰도를 밝히려고
             * 만든 값이 거짓말을 하는 셈이다.
             */
            it("센 달 수는 창의 길이가 아니라 기록이 있던 달의 수다") {
                val records =
                    listOf(
                        record(LocalDate.of(2026, 9, 1)),
                        record(LocalDate.of(2026, 7, 1)),
                    )

                records.averageMonthly(today, 6)?.months shouldBe 2
            }
        }

        /*
         * 문턱값과 갈래를 모델이 들고 있어야 타임라인과 목록이 갈리지 않는다.
         * 양쪽에 두었더니 같은 달을 두고 한쪽은 "많음", 다른 쪽은 "비슷"이 될 수 있었다.
         */
        describe("평소와 견주기") {
            it("뚜렷하게 많으면 MORE") {
                SpendingComparison.of(120_000) shouldBe SpendingComparison.MORE
            }

            it("뚜렷하게 적으면 LESS") {
                SpendingComparison.of(-120_000) shouldBe SpendingComparison.LESS
            }

            // 몇천 원 차이에 "많음"을 붙이면 매달 붙어, 정말 많이 쓴 달이 눈에 안 든다.
            it("문턱값 안쪽이면 SIMILAR") {
                SpendingComparison.of(SpendingComparison.NOISE_FLOOR) shouldBe SpendingComparison.SIMILAR
                SpendingComparison.of(-SpendingComparison.NOISE_FLOOR) shouldBe SpendingComparison.SIMILAR
                SpendingComparison.of(0) shouldBe SpendingComparison.SIMILAR
            }

            it("문턱값을 넘으면 갈린다") {
                SpendingComparison.of(SpendingComparison.NOISE_FLOOR + 1) shouldBe SpendingComparison.MORE
            }
        }
    })
