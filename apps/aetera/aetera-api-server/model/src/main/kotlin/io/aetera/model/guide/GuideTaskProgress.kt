package io.aetera.model.guide

import io.aetera.shared.error.CoreException
import java.time.Instant

/**
 * 할 일 하나에 대한 나의 상태. 사용자가 손댄 항목에만 행이 생긴다 —
 * 여정을 시작할 때 26개 행을 미리 깔아 두면 콘텐츠가 바뀔 때마다 저장된 행과 어긋난다.
 *
 * [note] 는 이 모듈이 정적인 블로그 글과 갈리는 지점이다.
 * "인사팀 김대리 확인함", "인수인계 문서는 노션 X 페이지" 같은 것을 항목 옆에 붙여 둘 수 있어야
 * 다시 열어 볼 이유가 생긴다.
 */
class GuideTaskProgress private constructor(
    val id: GuideTaskProgressId,
    val journeyId: GuideJourneyId,
    val taskKey: GuideTaskKey,
    done: Boolean,
    note: String?,
    updatedAt: Instant,
) {
    var done: Boolean = done
        private set

    var note: String? = note
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun update(
        done: Boolean,
        note: String?,
        at: Instant,
    ) {
        this.done = done
        this.note = validateNote(note)
        this.updatedAt = at
    }

    /** 체크도 안 했고 메모도 없으면 남길 이유가 없는 행이다. 지워서 진행 상태를 "손대기 전"으로 되돌린다. */
    val isBlank: Boolean get() = !done && note == null

    override fun equals(other: Any?): Boolean = this === other || (other is GuideTaskProgress && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "GuideTaskProgress(taskKey=$taskKey, done=$done)"

    companion object {
        private const val NOTE_MAX_LENGTH = 500

        fun create(
            id: GuideTaskProgressId,
            journeyId: GuideJourneyId,
            taskKey: GuideTaskKey,
            done: Boolean,
            note: String?,
            at: Instant,
        ): GuideTaskProgress = GuideTaskProgress(
            id = id,
            journeyId = journeyId,
            taskKey = taskKey,
            done = done,
            note = validateNote(note),
            updatedAt = at,
        )

        fun reconstitute(
            id: GuideTaskProgressId,
            journeyId: GuideJourneyId,
            taskKey: GuideTaskKey,
            done: Boolean,
            note: String?,
            updatedAt: Instant,
        ): GuideTaskProgress = GuideTaskProgress(id, journeyId, taskKey, done, note, updatedAt)

        private fun validateNote(note: String?): String? {
            val trimmed = note?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            if (trimmed.length > NOTE_MAX_LENGTH) {
                throw CoreException(
                    GuideErrorCode.NOTE_TOO_LONG,
                    "메모는 ${NOTE_MAX_LENGTH}자 이하여야 합니다. 입력 길이: ${trimmed.length}",
                )
            }
            return trimmed
        }
    }
}
