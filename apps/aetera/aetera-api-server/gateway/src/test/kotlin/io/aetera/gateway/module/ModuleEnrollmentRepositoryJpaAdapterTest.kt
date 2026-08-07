package io.aetera.gateway.module

import io.aetera.gateway.PostgresContainerConfig
import io.aetera.model.module.ModuleEnrollment
import io.aetera.model.module.ModuleEnrollmentId
import io.aetera.model.module.ModuleEnrollmentRepository
import io.aetera.model.module.ModuleId
import io.aetera.model.user.Email
import io.aetera.model.user.User
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Tag("integration")
@SpringBootTest
@Import(PostgresContainerConfig::class)
@Transactional
class ModuleEnrollmentRepositoryJpaAdapterTest {
    @Autowired
    private lateinit var moduleEnrollmentRepository: ModuleEnrollmentRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    private val now: Instant = Instant.parse("2026-01-01T00:00:00Z")

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    private fun persistUser(): UserId {
        val user =
            User.register(
                id = UserId.next(),
                email = Email("user-${UserId.next().value}@example.com"),
                nickname = "홍길동",
                registeredAt = now,
            )
        userRepository.save(user)
        return user.id
    }

    @Test
    fun `사용 상태를 왕복 저장하고 활성 여부를 존재 검사로 확인한다`() {
        val userId = persistUser()
        val moduleId = ModuleId("schedule")
        moduleEnrollmentRepository.save(
            ModuleEnrollment.enable(ModuleEnrollmentId.next(), userId, moduleId, now),
        )
        flushAndClear()

        assertThat(moduleEnrollmentRepository.existsEnabled(userId, moduleId)).isTrue()
        assertThat(moduleEnrollmentRepository.existsEnabled(userId, ModuleId("budget"))).isFalse()

        val found = requireNotNull(moduleEnrollmentRepository.getByUserIdAndModuleId(userId, moduleId))
        assertThat(found.isEnabled).isTrue()
        assertThat(found.enabledAt).isEqualTo(now)
    }

    @Test
    fun `비활성화하면 가드 검사에서 걸러진다`() {
        val userId = persistUser()
        val moduleId = ModuleId("schedule")
        moduleEnrollmentRepository.save(
            ModuleEnrollment.enable(ModuleEnrollmentId.next(), userId, moduleId, now),
        )
        flushAndClear()

        val enrollment = requireNotNull(moduleEnrollmentRepository.getByUserIdAndModuleId(userId, moduleId))
        enrollment.disable(now.plusSeconds(60))
        moduleEnrollmentRepository.save(enrollment)
        flushAndClear()

        assertThat(moduleEnrollmentRepository.existsEnabled(userId, moduleId)).isFalse()
        assertThat(moduleEnrollmentRepository.findAllByUserId(userId)).hasSize(1)
    }
}
