package io.aetera.usecase.guide

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.time.LocalDate

/**
 * 퇴사 준비 콘텐츠에만 해당하는 검증.
 * 모든 가이드가 지켜야 하는 규칙은 `GuideContentTest` 가 클래스패스를 훑어 확인한다.
 */
class ResignationGuideTest :
    DescribeSpec({
        val guide = RESIGNATION_GUIDE

        it("단계는 시간 순서대로 놓인다") {
            guide.phases.map { it.key } shouldContainExactly
                listOf("prepare", "notice", "wrapup", "after", "settle")
        }

        it("퇴사 전 단계는 D-day 이전에, 퇴사 후 단계는 이후에 놓인다 — 부호를 반대로 적는 실수를 잡는다") {
            fun offsetsOf(phaseKey: String) = guide.phases
                .single { it.key == phaseKey }
                .tasks
                .map { it.dueOffsetDays }

            (offsetsOf("prepare") + offsetsOf("notice")).forEach { it shouldBeLessThan 0 }
            (offsetsOf("after") + offsetsOf("settle")).forEach { it shouldBeGreaterThan 0 }
        }

        it("퇴직금은 퇴사 2주 뒤가 마감이다") {
            val severance = guide.tasks.single { it.key.value == "severance-pay" }

            severance.dueOffsetDays shouldBe 14
            severance.dueDateFrom(LocalDate.of(2026, 9, 30)) shouldBe LocalDate.of(2026, 10, 14)
        }
    })
