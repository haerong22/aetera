package io.aetera.model.guide

import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.LocalDate

class GuideJourneyTest :
    DescribeSpec({
        val now = Instant.parse("2026-08-14T00:00:00Z")
        val guideId = GuideId("resignation")

        fun journey(anchorDate: LocalDate = LocalDate.of(2026, 9, 30)) = GuideJourney.start(
            id = GuideJourneyId.next(),
            userId = UserId.next(),
            guideId = guideId,
            anchorDate = anchorDate,
            now = now,
        )

        describe("start") {
            it("기준일을 그대로 보관한다") {
                journey().anchorDate shouldBe LocalDate.of(2026, 9, 30)
            }

            it("지난 날짜도 기준일이 될 수 있다 — 이미 퇴사한 뒤에 시작하는 사람이 있다") {
                journey(LocalDate.of(2026, 7, 1)).anchorDate shouldBe LocalDate.of(2026, 7, 1)
            }

            it("상식 밖으로 먼 미래는 거절한다") {
                shouldThrow<CoreException> { journey(LocalDate.of(2206, 9, 30)) }
                    .errorCode shouldBe GuideErrorCode.INVALID_ANCHOR_DATE
            }

            it("상식 밖으로 먼 과거는 거절한다") {
                shouldThrow<CoreException> { journey(LocalDate.of(1998, 9, 30)) }
                    .errorCode shouldBe GuideErrorCode.INVALID_ANCHOR_DATE
            }
        }

        describe("changeAnchorDate") {
            it("퇴사일이 밀리면 기준일만 바뀐다 — 체크 상태는 별도 애그리거트라 영향받지 않는다") {
                val target = journey()

                target.changeAnchorDate(LocalDate.of(2026, 10, 31), now)

                target.anchorDate shouldBe LocalDate.of(2026, 10, 31)
                target.startedAt shouldBe now
            }

            it("바꾸려는 기준일도 같은 검증을 받는다") {
                val target = journey()

                shouldThrow<CoreException> { target.changeAnchorDate(LocalDate.of(2206, 1, 1), now) }
                    .errorCode shouldBe GuideErrorCode.INVALID_ANCHOR_DATE
                target.anchorDate shouldBe LocalDate.of(2026, 9, 30)
            }
        }
    })
