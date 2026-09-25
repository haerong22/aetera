package io.aetera.usecase.export

import io.aetera.model.module.ExportContributor
import io.aetera.model.user.Email
import io.aetera.model.user.User
import io.aetera.model.user.UserErrorCode
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * 내보내기가 지켜야 하는 약속들.
 *
 * 가장 중요한 것은 **꺼진 모듈도 함께 나온다**는 것이다. 모듈 스토어가 "중지해도 데이터는
 * 안전하게 남는다"고 약속하므로, 껐다고 빼면 그 약속이 거짓이 된다.
 */
class ExportMyDataServiceTest :
    DescribeSpec({
        val now = Instant.parse("2026-09-25T01:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)

        val owner = UserId.next()
        val user =
            User.reconstitute(
                id = owner,
                email = Email("hong@example.com"),
                nickname = "홍길동",
                timezone = ZoneOffset.UTC,
                status = io.aetera.model.user.UserStatus.ACTIVE,
                registeredAt = Instant.parse("2026-01-01T00:00:00Z"),
                withdrawnAt = null,
            )

        val userRepository = mockk<UserRepository>()

        /** 부르면 정해진 줄을 내놓는 기여자. */
        fun contributor(
            section: String,
            vararg rows: Map<String, Any?>,
        ) = object : ExportContributor {
            override val section = section

            override fun exportFor(userId: UserId) = rows.toList()
        }

        fun service(vararg contributors: ExportContributor) = ExportMyDataService(contributors.toList(), userRepository, clock)

        beforeEach {
            every { userRepository.getById(owner) } returns user
        }

        describe("프로필") {
            it("연락할 수 있는 정보와 가입 시점을 담는다") {
                val data = service().export(owner.value)

                data.profile["email"] shouldBe "hong@example.com"
                data.profile["nickname"] shouldBe "홍길동"
                data.profile["registeredAt"] shouldBe "2026-01-01T00:00:00Z"
            }

            // 여러 벌을 갖고 있을 때 어느 것이 최신인지 가려야 한다.
            it("언제 받아 간 것인지 적는다") {
                service().export(owner.value).exportedAt shouldBe now.toString()
            }

            it("없는 사용자면 거절한다") {
                every { userRepository.getById(owner) } returns null

                shouldThrow<CoreException> { service().export(owner.value) }
                    .errorCode shouldBe UserErrorCode.USER_NOT_FOUND
            }
        }

        describe("모듈 데이터") {
            it("기여자가 낸 줄을 섹션 이름 아래 그대로 담는다") {
                val data =
                    service(
                        contributor("asset", mapOf("name" to "통장", "amount" to 1000)),
                    ).export(owner.value)

                data.modules["asset"] shouldBe listOf(mapOf("name" to "통장", "amount" to 1000))
            }

            /*
             * 두 번 내려받은 파일을 비교할 수 있어야 한다. 빈에서 오는 순서는 정해져 있지 않아
             * 정렬하지 않으면 같은 데이터인데 파일이 매번 달라진다.
             */
            it("섹션은 이름순으로 고정한다") {
                val data =
                    service(
                        contributor("renewal"),
                        contributor("asset"),
                        contributor("income"),
                    ).export(owner.value)

                data.modules.keys.toList() shouldBe listOf("asset", "income", "renewal")
            }

            /*
             * 이 기능의 핵심 약속. 타임라인·알림 기여자와 달리 ExportContributor 에는
             * moduleIds 가 없어 켜짐을 묻지 않는다 — 여기가 그 사실을 지킨다.
             */
            it("켜짐을 묻지 않는다 — 꺼진 모듈의 데이터도 함께 나온다") {
                val data =
                    service(
                        contributor("resignation", mapOf("anchorDate" to "2026-11-30")),
                    ).export(owner.value)

                data.modules["resignation"] shouldBe listOf(mapOf("anchorDate" to "2026-11-30"))
            }

            // 한 번도 안 쓴 모듈이 목록에서 사라지면 "무엇을 담고 있는지" 를 알 수 없다.
            it("데이터가 없어도 섹션은 남긴다") {
                val data = service(contributor("goal")).export(owner.value)

                data.modules shouldContainKey "goal"
                data.modules["goal"] shouldBe emptyList()
            }

            it("기여자가 없으면 모듈 칸이 빈다") {
                service().export(owner.value).modules shouldBe emptyMap()
            }
        }
    })
