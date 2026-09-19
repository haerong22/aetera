package io.aetera.model.notification

import io.aetera.model.module.ModuleId
import io.aetera.model.module.Notice
import io.aetera.model.user.Email
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.LocalDate

/**
 * 사용자에게 그대로 나가는 글이라 문구를 못 박는다.
 *
 * 로그 발송기와 메일 발송기가 **같은 글**을 써야 한다는 것이 이 클래스의 존재 이유다 —
 * 둘이 갈리면 로그를 보고 확인하는 일 자체가 쓸모없어진다.
 */
class NoticeDigestTest :
    DescribeSpec({
        val today = LocalDate.of(2026, 9, 19)

        fun notice(
            title: String = "실손보험 만기",
            on: LocalDate = LocalDate.of(2026, 10, 5),
            detail: String? = "16일 남았어요",
        ) = Notice(moduleId = ModuleId("renewal"), on = on, title = title, detail = detail)

        fun digest(vararg notices: Notice) = NoticeDigest(
            to = Email("hong@example.com"),
            nickname = "홍길동",
            on = today,
            notices = notices.toList(),
        )

        describe("제목") {
            // "알림 1건"은 열어 봐야만 뜻이 생긴다. 받은 편지함에서 바로 알아보게 한다.
            it("한 건이면 그 건을 그대로 적는다") {
                digest(notice()).subject shouldBe "[아이테라] 실손보험 만기 (16일 남았어요)"
            }

            it("여러 건이면 첫 건에 나머지 수를 붙인다") {
                digest(notice(), notice(title = "여권 만기"), notice(title = "계약 만기"))
                    .subject shouldBe "[아이테라] 실손보험 만기 (16일 남았어요) 외 2건"
            }

            it("곁들이는 말이 없으면 제목만") {
                digest(notice(detail = null)).subject shouldBe "[아이테라] 실손보험 만기"
            }
        }

        describe("본문") {
            it("이름을 부르고, 줄마다 날짜와 함께 적는다") {
                digest(notice()).body shouldBe
                    """
                    홍길동님, 오늘 챙길 것을 모았어요.

                    · 10월 5일  실손보험 만기 — 16일 남았어요

                    알림을 그만 받으려면 아이테라 설정에서 끌 수 있어요.
                    """.trimIndent()
            }

            it("날짜는 앞의 0을 떼고 읽는다") {
                digest(notice(on = LocalDate.of(2026, 1, 3))).body shouldContain "· 1월 3일"
            }

            it("곁들이는 말이 없으면 줄표도 없다") {
                digest(notice(detail = null)).body shouldContain "· 10월 5일  실손보험 만기\n"
            }

            it("여러 줄이 순서대로 들어간다") {
                val body = digest(notice(), notice(title = "여권 만기", detail = null)).body
                body shouldContain "실손보험 만기"
                body shouldContain "여권 만기"
            }

            // 끄는 길을 매번 알린다 — 알림이 성가셔졌을 때 찾아 헤매게 하면 스팸과 다를 게 없다.
            it("끄는 방법을 늘 덧붙인다") {
                digest(notice()).body shouldContain "설정에서 끌 수 있어요"
            }
        }

        describe("빈 다이제스트") {
            // "오늘은 알릴 게 없습니다"를 매일 보내는 순간 이 기능은 스팸이 된다.
            it("아예 만들 수 없다") {
                shouldThrow<IllegalArgumentException> { digest() }
            }
        }
    })
