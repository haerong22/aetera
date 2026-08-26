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
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class EnableModuleServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)

        val scheduleDescriptor =
            ModuleDescriptor(
                id = ModuleId("schedule"),
                displayName = "일정",
                description = "일정 관리",
                category = ModuleCategory.TOOL,
            )
        val registry =
            ModuleRegistry(
                listOf(
                    object : AeteraModule {
                        override val descriptor = scheduleDescriptor
                    },
                ),
            )
        val enrollmentRepository = mockk<ModuleEnrollmentRepository>()
        val enableService = EnableModuleService(registry, enrollmentRepository, clock)
        val disableService = DisableModuleService(registry, enrollmentRepository, clock)
        val accessService = ModuleAccessService(registry, enrollmentRepository)

        val userId = UserId.next()

        beforeTest {
            clearMocks(enrollmentRepository)
            every { enrollmentRepository.save(any()) } answers { firstArg() }
        }

        describe("enable") {
            it("처음이면 새 사용 이력을 만들어 활성화한다") {
                every { enrollmentRepository.getByUserIdAndModuleId(userId, ModuleId("schedule")) } returns null

                val result = enableService.enable(userId.value, "schedule")

                result.enabled shouldBe true
                result.enabledAt shouldBe now
            }

            it("비활성 이력이 있으면 그 행을 되살린다 — 데이터가 돌아온다") {
                val dormant =
                    ModuleEnrollment.reconstitute(
                        id = ModuleEnrollmentId.next(),
                        userId = userId,
                        moduleId = ModuleId("schedule"),
                        status = EnrollmentStatus.DISABLED,
                        enabledAt = now.minusSeconds(86_400),
                        disabledAt = now.minusSeconds(3600),
                        sortOrder = ModuleEnrollment.DEFAULT_SORT_ORDER,
                    )
                every { enrollmentRepository.getByUserIdAndModuleId(userId, ModuleId("schedule")) } returns dormant

                val result = enableService.enable(userId.value, "schedule")

                result.enabled shouldBe true
                dormant.status shouldBe EnrollmentStatus.ENABLED
            }

            it("배포되지 않은 모듈은 켤 수 없다") {
                shouldThrow<CoreException> { enableService.enable(userId.value, "budget") }
                    .errorCode shouldBe ModuleErrorCode.MODULE_NOT_FOUND
            }
        }

        describe("disable") {
            it("사용 이력이 없어도 조용히 성공한다") {
                every { enrollmentRepository.getByUserIdAndModuleId(userId, ModuleId("schedule")) } returns null

                disableService.disable(userId.value, "schedule").enabled shouldBe false
            }
        }

        describe("checkAccess") {
            it("활성화한 모듈이면 통과한다") {
                every { enrollmentRepository.existsEnabled(userId, ModuleId("schedule")) } returns true

                accessService.checkAccess(userId.value, "schedule")
            }

            it("활성화하지 않은 모듈이면 MODULE_NOT_ENABLED 로 거절한다") {
                every { enrollmentRepository.existsEnabled(userId, ModuleId("schedule")) } returns false

                shouldThrow<CoreException> { accessService.checkAccess(userId.value, "schedule") }
                    .errorCode shouldBe ModuleErrorCode.MODULE_NOT_ENABLED
            }
        }

        describe("ModuleRegistry") {
            it("모듈 아이디가 겹치면 기동에 실패한다") {
                shouldThrow<IllegalStateException> {
                    ModuleRegistry(
                        listOf(
                            object : AeteraModule {
                                override val descriptor = scheduleDescriptor
                            },
                            object : AeteraModule {
                                override val descriptor = scheduleDescriptor.copy(displayName = "짝퉁 일정")
                            },
                        ),
                    )
                }
            }
        }
    })
