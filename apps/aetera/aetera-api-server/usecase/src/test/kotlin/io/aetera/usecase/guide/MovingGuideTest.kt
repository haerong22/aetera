package io.aetera.usecase.guide

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe

/**
 * 이사 준비 콘텐츠에만 해당하는 검증.
 * 모든 가이드가 지켜야 하는 규칙은 `GuideContentTest` 가 클래스패스를 훑어 확인한다.
 */
class MovingGuideTest :
    DescribeSpec({
        val guide = MOVING_GUIDE

        it("단계는 시간 순서대로 놓인다") {
            guide.phases.map { it.key } shouldContainExactly
                listOf("contract", "booking", "prepare", "movingday", "settle")
        }

        it("이사 전 단계는 D-day 이전, 당일 단계는 0, 이후 단계는 그 뒤에 놓인다") {
            fun offsetsOf(phaseKey: String) = guide.phases
                .single { it.key == phaseKey }
                .tasks
                .map { it.dueOffsetDays }

            (offsetsOf("contract") + offsetsOf("booking") + offsetsOf("prepare")).forEach { it shouldBeLessThan 0 }
            offsetsOf("movingday").forEach { it shouldBe 0 }
            offsetsOf("settle").forEach { it shouldBeGreaterThan 0 }
        }

        it("보증금을 지키는 항목은 이사 직후가 마감이다 — 늦으면 대항력이 생기지 않는다") {
            guide.tasks.single { it.key.value == "move-in-report" }.dueOffsetDays shouldBe 1
            guide.tasks.single { it.key.value == "fixed-date" }.dueOffsetDays shouldBe 1
        }

        it("보증금 반환은 열쇠를 넘기기 전에 확인한다") {
            val deposit = guide.tasks.single { it.key.value == "deposit-return" }
            val key = guide.tasks.single { it.key.value == "key-handover" }

            guide.tasks.indexOf(deposit) shouldBeLessThan guide.tasks.indexOf(key)
        }
    })
