package io.aetera.usecase.guide

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.string.shouldStartWith
import java.time.LocalDate

/**
 * 콘텐츠 검증. 이 모듈에서 가장 깨지기 쉬운 것은 로직이 아니라 콘텐츠다 —
 * 항목을 붙여 넣다 키가 겹치거나, 오프셋 부호를 반대로 적거나(퇴사 "전" 항목이 "후"로 가거나),
 * 필수 표시를 빠뜨리는 실수는 컴파일러가 잡아주지 않는다.
 */
class ResignationGuideTest :
    DescribeSpec({
        val guide = RESIGNATION_GUIDE

        it("할 일 키는 가이드 안에서 유일하다 — 겹치면 두 항목이 한 체크 상태를 공유한다") {
            val duplicated =
                guide.tasks
                    .groupingBy { it.key }
                    .eachCount()
                    .filterValues { it > 1 }
                    .keys
            duplicated.shouldBeEmpty()
        }

        it("단계는 시간 순서대로 놓인다") {
            guide.phases.map { it.key } shouldContainExactly
                listOf("prepare", "notice", "wrapup", "after", "settle")
        }

        it("할 일은 마감이 이른 것부터 나온다 — 화면이 곧 시간표라 순서가 흐트러지면 안 된다") {
            val offsets = guide.tasks.map { it.dueOffsetDays }
            offsets shouldBe offsets.sorted()
        }

        it("퇴사 전 단계는 D-day 이전에, 퇴사 후 단계는 이후에 놓인다 — 부호를 반대로 적는 실수를 잡는다") {
            fun offsetsOf(phaseKey: String) = guide.phases
                .single { it.key == phaseKey }
                .tasks
                .map { it.dueOffsetDays }

            (offsetsOf("prepare") + offsetsOf("notice")).forEach { it shouldBeLessThan 0 }
            (offsetsOf("after") + offsetsOf("settle")).forEach { it shouldBeGreaterThan 0 }
        }

        it("필수 항목이 있고, 전부 필수는 아니다 — 참고용까지 필수면 진행률이 지표가 못 된다") {
            guide.requiredTaskCount shouldBeGreaterThan 0
            guide.tasks.size shouldBeGreaterThan guide.requiredTaskCount
        }

        it("모든 항목이 제목과 설명을 갖는다") {
            guide.tasks.forEach {
                it.title.shouldNotBeBlank()
                it.description.shouldNotBeBlank()
            }
        }

        it("링크는 https 로만 건다") {
            guide.tasks.mapNotNull { it.link }.forEach {
                it.url.shouldStartWith("https://")
                it.label.shouldNotBeBlank()
            }
        }

        it("기준일을 넣으면 각 항목의 마감이 내 달력의 실제 날짜가 된다") {
            val anchor = LocalDate.of(2026, 9, 30)
            val severance = guide.tasks.single { it.key.value == "severance-pay" }

            severance.dueOffsetDays shouldBe 14
            severance.dueDateFrom(anchor) shouldBe LocalDate.of(2026, 10, 14)
        }
    })
