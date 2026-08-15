package io.aetera.usecase.guide

import io.aetera.model.guide.GuideErrorCode
import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideJourneyRepository
import io.aetera.model.guide.GuideTaskKey
import io.aetera.model.guide.GuideTaskProgress
import io.aetera.model.guide.GuideTaskProgressId
import io.aetera.model.guide.GuideTaskProgressRepository
import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.aetera.usecase.guide.cmd.UpdateGuideTaskCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class UpdateGuideTaskService(
    private val guideCatalog: GuideCatalog,
    private val guideJourneyRepository: GuideJourneyRepository,
    private val guideTaskProgressRepository: GuideTaskProgressRepository,
    private val findGuideService: FindGuideService,
    private val clock: Clock,
) {
    /** 체크와 메모를 한 번에 저장한다. 여정을 시작하지 않았다면 저장할 곳이 없으므로 거절한다. */
    @Transactional
    fun updateTask(command: UpdateGuideTaskCommand): GuideViewDto {
        val template = guideCatalog.getOrThrow(GuideId(command.guideId))

        // 경로에 이상한 값이 오는 것은 "형식 오류"가 아니라 "가이드에 없는 할 일"이다.
        // 형식만 400 으로 따로 내보내면 같은 상황에 두 가지 응답이 나간다.
        val taskKey =
            GuideTaskKey.parseOrNull(command.taskKey)?.takeIf(template::hasTask)
                ?: throw CoreException(
                    GuideErrorCode.TASK_NOT_FOUND,
                    "가이드 '${template.id}' 에 없는 할 일입니다. taskKey=${command.taskKey}",
                )

        val journey =
            guideJourneyRepository.getByUserIdAndGuideId(UserId(command.userId), template.id)
                ?: throw CoreException(
                    GuideErrorCode.JOURNEY_NOT_STARTED,
                    "${template.anchorLabel}을 먼저 정해 주세요. guideId=${template.id}",
                )

        val now = clock.instant()
        val existing = guideTaskProgressRepository.getByJourneyIdAndTaskKey(journey.id, taskKey)

        when {
            existing != null -> {
                existing.update(command.done, command.note, now)
                // 체크도 풀고 메모도 지웠으면 남길 것이 없다. 행을 지워 "손대기 전"으로 되돌린다.
                if (existing.isBlank) {
                    guideTaskProgressRepository.delete(existing)
                } else {
                    guideTaskProgressRepository.save(existing)
                }
            }

            // 없던 항목을 빈 상태로 저장하지 않는다 — 스크롤만 해도 행이 쌓이는 것을 막는다.
            command.done || !command.note.isNullOrBlank() -> {
                guideTaskProgressRepository.save(
                    GuideTaskProgress.create(
                        id = GuideTaskProgressId.next(),
                        journeyId = journey.id,
                        taskKey = taskKey,
                        done = command.done,
                        note = command.note,
                        at = now,
                    ),
                )
            }
        }

        return findGuideService.viewOf(template, journey)
    }
}
