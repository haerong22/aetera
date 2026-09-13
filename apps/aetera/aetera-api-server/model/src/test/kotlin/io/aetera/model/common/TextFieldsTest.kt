package io.aetera.model.common

import io.aetera.model.expense.ExpenseErrorCode
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * 여섯 모델이 함께 쓰는 함수라 **문구까지 고정한다.**
 *
 * 코드만 보는 테스트로는 조사가 틀려도("메모은") 통과한다. 사용자에게 그대로 나가는 문장이므로
 * 여기서 한 번 못 박아 두면 부르는 쪽마다 다시 확인하지 않아도 된다.
 *
 * 오류 코드는 아무 도메인 것이나 빌려 쓴다 — 이 함수는 받은 코드를 그대로 되던질 뿐이라
 * 어느 것을 넣는지가 결과를 바꾸지 않는다.
 */
class TextFieldsTest :
    DescribeSpec({
        val code = ExpenseErrorCode.INVALID_TITLE

        describe("requiredText") {
            it("앞뒤 공백을 턴다") {
                requiredText("  월세  ", 100, code, "이름") shouldBe "월세"
            }

            it("공백만 있으면 빈 값으로 본다") {
                shouldThrow<CoreException> { requiredText("   ", 100, code, "이름") }
                    .message shouldBe "이름은 1자 이상 100자 이하여야 합니다. 입력 길이: 0"
            }

            it("상한은 턴 뒤의 길이로 잰다") {
                requiredText(" ${"가".repeat(100)} ", 100, code, "이름").length shouldBe 100
            }

            it("넘치면 막고 길이를 알려준다") {
                shouldThrow<CoreException> { requiredText("가".repeat(101), 100, code, "이름") }
                    .message shouldBe "이름은 1자 이상 100자 이하여야 합니다. 입력 길이: 101"
            }
        }

        describe("optionalText") {
            it("없으면 null") {
                optionalText(null, 500, code, "메모") shouldBe null
            }

            it("공백만 쳤으면 null 로 접는다") {
                optionalText("   ", 500, code, "메모") shouldBe null
            }

            it("넘치면 막는다") {
                shouldThrow<CoreException> { optionalText("가".repeat(501), 500, code, "메모") }
                    .message shouldBe "메모는 500자 이하여야 합니다. 입력 길이: 501"
            }
        }

        describe("조사") {
            it("받침이 있으면 은") {
                shouldThrow<CoreException> { requiredText("", 10, code, "일정 제목") }
                    .message
                    .startsWith("일정 제목은 ") shouldBe true
            }

            it("받침이 없으면 는") {
                shouldThrow<CoreException> { optionalText("가".repeat(11), 10, code, "단위") }
                    .message
                    .startsWith("단위는 ") shouldBe true
            }
        }
    })
