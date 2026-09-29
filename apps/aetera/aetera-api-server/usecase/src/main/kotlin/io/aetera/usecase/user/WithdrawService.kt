package io.aetera.usecase.user

import io.aetera.model.auth.AuthCredentialRepository
import io.aetera.model.auth.PasswordResetTokenRepository
import io.aetera.model.auth.RefreshTokenRepository
import io.aetera.model.module.ModuleEnrollmentRepository
import io.aetera.model.module.UserDataContributor
import io.aetera.model.notification.NotificationPreferenceRepository
import io.aetera.model.user.UserErrorCode
import io.aetera.model.user.UserId
import io.aetera.model.user.UserRepository
import io.aetera.shared.error.CoreException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * 탈퇴 — 이 사람의 것을 전부 지운다.
 *
 * **상태만 바꾸지 않는다.** 그렇게 두면 11개 모듈의 돈 이야기가 그대로 남고, 이메일
 * 유니크 인덱스가 그 주소를 묶어 같은 메일로 다시 올 수도 없다. "지워 달라"는 말에
 * "안 보이게 해 두었다"로 답하는 셈이다.
 *
 * 가져갈 것이 있으면 [ExportMyDataService][io.aetera.usecase.export.ExportMyDataService] 로
 * 먼저 받아 두면 된다. 화면이 그 순서를 안내한다.
 *
 * 한 트랜잭션에서 지운다 — 중간에 실패하면 반쯤 지워진 계정이 남는데, 그건 데이터가
 * 남은 것보다 나쁘다. 어느 모듈이 사라졌는지 아무도 모른다.
 */
@Service
class WithdrawService(
    private val contributors: List<UserDataContributor>,
    private val userRepository: UserRepository,
    private val authCredentialRepository: AuthCredentialRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val moduleEnrollmentRepository: ModuleEnrollmentRepository,
    private val notificationPreferenceRepository: NotificationPreferenceRepository,
) {
    @Transactional
    fun withdraw(userId: UUID) {
        val owner = UserId(userId)
        val user =
            userRepository.getById(owner)
                ?: throw CoreException(UserErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다. id=$owner")

        // 모듈 데이터. 어떤 모듈이 있는지 모르지만, 데이터를 가진 모듈은 반드시 여기 있다.
        contributors.forEach { it.deleteAllFor(owner) }

        // 코어가 들고 있는 것들. 모듈이 아니라 기여자가 없다.
        moduleEnrollmentRepository.deleteAllByUserId(owner)
        notificationPreferenceRepository.deleteAllByUserId(owner)

        /*
         * 인증은 마지막에서 두 번째다. 먼저 지우면 이 요청의 나머지가 권한을 잃는 것은
         * 아니지만(이미 인증을 통과했다), 실패해 롤백됐을 때 남는 그림이 덜 헷갈린다.
         */
        refreshTokenRepository.deleteAllByUserId(owner)
        passwordResetTokenRepository.deleteAllByUserId(owner)
        authCredentialRepository.deleteAllByUserId(owner)

        userRepository.delete(user)
        log.info { "탈퇴 처리 완료 userId=$owner sections=${contributors.size}" }
    }
}
