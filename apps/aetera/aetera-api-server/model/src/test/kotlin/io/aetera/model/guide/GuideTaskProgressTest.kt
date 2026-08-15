package io.aetera.model.guide

import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant

class GuideTaskProgressTest :
    DescribeSpec({
        val now = Instant.parse("2026-08-14T00:00:00Z")

        fun progress(
            done: Boolean = true,
            note: String? = null,
        ) = GuideTaskProgress.create(
            id = GuideTaskProgressId.next(),
            journeyId = GuideJourneyId.next(),
            taskKey = GuideTaskKey("severance-pay"),
            done = done,
            note = note,
            at = now,
        )

        it("메모 앞뒤 공백은 제거된다") {
            progress(note = "  인사팀 확인함  ").note shouldBe "인사팀 확인함"
        }

        it("공백뿐인 메모는 없는 것으로 본다") {
            progress(note = "   ").note.shouldBeNull()
        }

        it("너무 긴 메모는 거절한다") {
            shouldThrow<CoreException> { progress(note = "가".repeat(501)) }
                .errorCode shouldBe GuideErrorCode.NOTE_TOO_LONG
        }

        it("체크도 메모도 없으면 남길 이유가 없는 행이다") {
            progress(done = false, note = null).isBlank shouldBe true
            progress(done = false, note = "메모만 남김").isBlank shouldBe false
            progress(done = true, note = null).isBlank shouldBe false
        }

        it("update 는 체크·메모·시각을 함께 바꾼다") {
            val target = progress(done = false)
            val later = now.plusSeconds(60)

            target.update(done = true, note = "완료", at = later)

            target.done shouldBe true
            target.note shouldBe "완료"
            target.updatedAt shouldBe later
        }
    })
