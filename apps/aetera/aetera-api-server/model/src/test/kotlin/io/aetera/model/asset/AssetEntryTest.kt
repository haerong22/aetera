package io.aetera.model.asset

import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.LocalDate

class AssetEntryTest :
    DescribeSpec({
        val today = LocalDate.of(2026, 9, 1)
        val now = Instant.parse("2026-09-01T00:00:00Z")
        val userId = UserId.next()

        fun entry(
            name: String = "주거래 통장",
            category: AssetCategory = AssetCategory.CASH,
            amount: Long = 12_000_000L,
            month: LocalDate = today,
        ) = AssetEntry.create(
            id = AssetEntryId.next(),
            userId = userId,
            month = month,
            name = name,
            category = category,
            amount = amount,
            today = today,
            recordedAt = now,
        )

        describe("달") {
            it("며칠을 보내든 그 달의 1일로 맞춘다") {
                entry(month = LocalDate.of(2026, 9, 30)).month shouldBe LocalDate.of(2026, 9, 1)
            }

            it("너무 먼 달은 거절한다") {
                shouldThrow<CoreException> { entry(month = today.plusYears(31)) }
                    .errorCode shouldBe AssetErrorCode.INVALID_MONTH
            }
        }

        describe("순자산") {
            it("부채는 빼는 쪽이다") {
                entry(category = AssetCategory.DEBT, amount = 75_800_000L).signedAmount shouldBe -75_800_000L
                entry(category = AssetCategory.CASH, amount = 75_800_000L).signedAmount shouldBe 75_800_000L
            }

            it("가진 것에서 갚을 것을 뺀다") {
                val entries =
                    listOf(
                        entry(name = "통장", category = AssetCategory.CASH, amount = 12_000_000L),
                        entry(name = "주식", category = AssetCategory.INVESTMENT, amount = 24_000_000L),
                        entry(name = "전세보증금", category = AssetCategory.REAL_ESTATE, amount = 80_000_000L),
                        entry(name = "전세대출", category = AssetCategory.DEBT, amount = 75_800_000L),
                    )

                entries.netWorth() shouldBe 40_200_000L
            }

            it("항목이 없으면 0 이다") {
                emptyList<AssetEntry>().netWorth() shouldBe 0L
            }

            it("빚이 더 많으면 음수가 된다 — 사실을 감추지 않는다") {
                val entries =
                    listOf(
                        entry(category = AssetCategory.CASH, amount = 1_000_000L),
                        entry(category = AssetCategory.DEBT, amount = 50_000_000L),
                    )

                entries.netWorth() shouldBe -49_000_000L
            }
        }

        describe("당장 쓸 수 있는 돈") {
            it("현금만 센다 — 집을 팔아 버틴다고 셈하지 않는다") {
                val entries =
                    listOf(
                        entry(name = "통장", category = AssetCategory.CASH, amount = 12_000_000L),
                        entry(name = "적금", category = AssetCategory.CASH, amount = 8_000_000L),
                        entry(name = "주식", category = AssetCategory.INVESTMENT, amount = 24_000_000L),
                        entry(name = "전세보증금", category = AssetCategory.REAL_ESTATE, amount = 80_000_000L),
                        entry(name = "전세대출", category = AssetCategory.DEBT, amount = 75_800_000L),
                    )

                entries.cashTotal() shouldBe 20_000_000L
            }
        }

        describe("입력 검증") {
            it("빈 이름은 거절한다") {
                shouldThrow<CoreException> { entry(name = "   ") }
                    .errorCode shouldBe AssetErrorCode.INVALID_NAME
            }

            it("잔액 0 은 받는다 — 갖고는 있는 계좌다") {
                entry(amount = 0L).amount shouldBe 0L
            }

            it("음수는 거절한다 — 부채는 분류로 표시한다") {
                shouldThrow<CoreException> { entry(amount = -1L) }
                    .errorCode shouldBe AssetErrorCode.INVALID_AMOUNT
            }

            it("터무니없이 큰 금액은 거절한다") {
                shouldThrow<CoreException> { entry(amount = 1_000_000_000_001L) }
                    .errorCode shouldBe AssetErrorCode.INVALID_AMOUNT
            }
        }
    })
