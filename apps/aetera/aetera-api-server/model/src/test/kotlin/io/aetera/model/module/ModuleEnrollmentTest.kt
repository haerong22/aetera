package io.aetera.model.module

import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant

class ModuleEnrollmentTest :
    DescribeSpec({
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val later = now.plusSeconds(3600)

        fun enrollment() = ModuleEnrollment.enable(
            id = ModuleEnrollmentId.next(),
            userId = UserId.next(),
            moduleId = ModuleId("schedule"),
            at = now,
        )

        describe("ModuleId") {
            it("소문자-대시 형식만 허용한다") {
                ModuleId("schedule").value shouldBe "schedule"
                ModuleId("exit-guide").value shouldBe "exit-guide"

                shouldThrow<CoreException> { ModuleId("Schedule") }.errorCode shouldBe ModuleErrorCode.INVALID_MODULE_ID
                shouldThrow<CoreException> { ModuleId("1module") }.errorCode shouldBe ModuleErrorCode.INVALID_MODULE_ID
                shouldThrow<CoreException> { ModuleId("") }.errorCode shouldBe ModuleErrorCode.INVALID_MODULE_ID
            }
        }

        describe("활성화/비활성화") {
            it("처음 활성화하면 ENABLED 상태로 시작한다") {
                val sut = enrollment()

                sut.isEnabled shouldBe true
                sut.enabledAt shouldBe now
                sut.disabledAt.shouldBeNull()
            }

            it("비활성화는 데이터를 남기고 접근만 막는다 — 시각을 기록한다") {
                val sut = enrollment()

                sut.disable(later)

                sut.isEnabled shouldBe false
                sut.disabledAt shouldBe later
            }

            it("다시 활성화하면 비활성 시각이 지워진다") {
                val sut = enrollment()
                sut.disable(later)

                sut.enable(later.plusSeconds(60))

                sut.isEnabled shouldBe true
                sut.enabledAt shouldBe later.plusSeconds(60)
                sut.disabledAt.shouldBeNull()
            }

            it("활성화는 몇 번을 반복해도 같은 결과다") {
                val sut = enrollment()

                sut.enable(later)

                sut.enabledAt shouldBe now // 이미 활성 — 시각이 덮이지 않는다
            }
        }

        describe("순서") {
            it("처음 활성화하면 순서를 정한 적 없는 자리에 놓인다") {
                enrollment().sortOrder shouldBe ModuleEnrollment.DEFAULT_SORT_ORDER
            }

            it("순서만 정한 모듈에는 사용 이력이 남지 않는다") {
                val sut =
                    ModuleEnrollment.forOrder(
                        id = ModuleEnrollmentId.next(),
                        userId = UserId.next(),
                        moduleId = ModuleId("schedule"),
                        at = now,
                        sortOrder = 3,
                    )

                sut.isEnabled shouldBe false
                sut.sortOrder shouldBe 3
                // 켰다가 끈 것과 구분된다 — 끈 시각이 없다.
                sut.disabledAt.shouldBeNull()
            }

            it("순서를 바꿔도 켜짐 상태는 그대로다") {
                val sut = enrollment()

                sut.changeSortOrder(0)

                sut.sortOrder shouldBe 0
                sut.isEnabled shouldBe true
                sut.enabledAt shouldBe now
            }
        }
    })
