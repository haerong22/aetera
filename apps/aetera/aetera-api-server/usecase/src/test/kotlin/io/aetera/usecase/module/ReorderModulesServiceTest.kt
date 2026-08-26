package io.aetera.usecase.module

import io.aetera.model.module.AeteraModule
import io.aetera.model.module.EnrollmentStatus
import io.aetera.model.module.ModuleCategory
import io.aetera.model.module.ModuleDescriptor
import io.aetera.model.module.ModuleEnrollment
import io.aetera.model.module.ModuleEnrollmentId
import io.aetera.model.module.ModuleEnrollmentRepository
import io.aetera.model.module.ModuleErrorCode
import io.aetera.model.module.ModuleId
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ReorderModulesServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)

        val registry =
            ModuleRegistry(
                listOf("schedule" to "일정", "goal" to "목표", "renewal" to "만기 관리").map { (id, name) ->
                    object : AeteraModule {
                        override val descriptor =
                            ModuleDescriptor(
                                id = ModuleId(id),
                                displayName = name,
                                description = "$name 모듈",
                                category = ModuleCategory.TOOL,
                            )
                    }
                },
            )
        val enrollmentRepository = mockk<ModuleEnrollmentRepository>()
        val service = ReorderModulesService(registry, enrollmentRepository, clock)

        val userId = UserId.next()
        val saved = mutableListOf<ModuleEnrollment>()

        fun enrolled(
            moduleId: String,
            status: EnrollmentStatus,
            sortOrder: Int,
        ) = ModuleEnrollment.reconstitute(
            id = ModuleEnrollmentId.next(),
            userId = userId,
            moduleId = ModuleId(moduleId),
            status = status,
            enabledAt = now.minusSeconds(86_400),
            disabledAt = if (status == EnrollmentStatus.ENABLED) null else now.minusSeconds(3600),
            sortOrder = sortOrder,
        )

        beforeTest {
            clearMocks(enrollmentRepository)
            saved.clear()
            every { enrollmentRepository.save(any()) } answers { firstArg<ModuleEnrollment>().also { saved += it } }
            every { enrollmentRepository.findAllByUserId(userId) } returns emptyList()
        }

        describe("reorder") {
            it("보낸 순서대로 놓는다") {
                val result = service.reorder(userId.value, listOf("renewal", "goal", "schedule"))

                result.map { it.id } shouldBe listOf("renewal", "goal", "schedule")
            }

            it("목록에 없는 모듈은 건드리지 않고 뒤에 남긴다") {
                val result = service.reorder(userId.value, listOf("renewal"))

                result.map { it.id } shouldBe listOf("renewal", "goal", "schedule")
                saved.map { it.moduleId.value } shouldBe listOf("renewal")
            }

            it("켠 적 없는 모듈의 순서를 정해도 사용 이력이 남지 않는다") {
                val result = service.reorder(userId.value, listOf("goal"))

                saved.single().status shouldBe EnrollmentStatus.DISABLED
                saved.single().disabledAt.shouldBeNull()
                result.first { it.id == "goal" }.enabled shouldBe false
                result.first { it.id == "goal" }.enabledAt.shouldBeNull()
            }

            it("같은 모듈이 두 번 와도 행을 하나만 만든다 — 유니크 제약에 걸리지 않는다") {
                val result = service.reorder(userId.value, listOf("goal", "goal", "renewal"))

                saved.map { it.moduleId.value } shouldBe listOf("goal", "renewal")
                result.map { it.id } shouldBe listOf("goal", "renewal", "schedule")
            }

            it("이미 켠 모듈은 순서만 바뀌고 켜진 채로 남는다") {
                val enabled = enrolled("schedule", EnrollmentStatus.ENABLED, ModuleEnrollment.DEFAULT_SORT_ORDER)
                every { enrollmentRepository.findAllByUserId(userId) } returns listOf(enabled)

                val result = service.reorder(userId.value, listOf("schedule", "goal"))

                enabled.sortOrder shouldBe 0
                enabled.status shouldBe EnrollmentStatus.ENABLED
                result.map { it.id } shouldBe listOf("schedule", "goal", "renewal")
                result.first().enabled shouldBe true
            }

            it("방금 저장한 값을 그대로 쓴다 — 다시 읽지 않는다") {
                service.reorder(userId.value, listOf("goal", "renewal"))

                verify(exactly = 1) { enrollmentRepository.findAllByUserId(userId) }
            }

            it("배포되지 않은 모듈은 순서를 정할 수 없다") {
                shouldThrow<CoreException> { service.reorder(userId.value, listOf("budget")) }
                    .errorCode shouldBe ModuleErrorCode.MODULE_NOT_FOUND
            }
        }
    })
