package io.aetera.usecase.export

import io.aetera.model.module.ExportContributor
import io.aetera.model.user.UserErrorCode
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.shared.error.CoreException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

/**
 * 내 데이터를 통째로 내놓는다.
 *
 * **떠날 자유가 없으면 들어오기도 망설인다.** 돈 이야기를 몇 달 적어 두고 꺼낼 길이 없으면
 * 그건 사용자의 기록이 아니라 우리 것이 된다.
 *
 * 어떤 모듈이 있는지 모른다 — 스프링이 [ExportContributor] 빈을 전부 넣어 주므로
 * 새 모듈이 생겨도 여기는 바뀌지 않는다(타임라인·알림과 같은 방식).
 *
 * 꺼진 모듈도 함께 낸다. 모듈 스토어가 "중지해도 데이터는 남는다"고 약속하므로,
 * 껐다고 빼면 그 약속이 거짓이 된다.
 */
@Service
@Transactional(readOnly = true)
class ExportMyDataService(
    private val contributors: List<ExportContributor>,
    private val userRepository: UserRepository,
    private val clock: Clock,
) {
    fun export(userId: UUID): MyDataDto {
        val owner = UserId(userId)
        val user =
            userRepository.getById(owner)
                ?: throw CoreException(UserErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다. id=$owner")

        return MyDataDto(
            exportedAt = clock.instant().toString(),
            profile =
                mapOf(
                    "email" to user.email.value,
                    "nickname" to user.nickname,
                    "timezone" to user.timezone.id,
                    "registeredAt" to user.registeredAt.toString(),
                ),
            // 섹션 이름순으로 고정한다 — 두 번 내려받은 파일을 비교할 수 있어야 한다.
            modules =
                contributors
                    .sortedBy { it.section }
                    .associate { it.section to it.exportFor(owner) },
        )
    }
}
