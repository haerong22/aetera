package io.aetera.usecase.spending

import io.aetera.model.module.ModuleId
import io.aetera.model.module.Notice
import io.aetera.model.module.NotificationContributor
import io.aetera.model.spending.SpendingRecord
import io.aetera.model.spending.SpendingRecordRepository
import io.aetera.model.user.UserId
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 지난 달을 아직 안 적었으면 한 번 알린다.
 *
 * **이 모듈은 적는 것을 잊으면 아무 값이 없다.** 달에 한 번이라 오히려 잊기 쉽고, 잊으면
 * 평균이 낡은 채로 퇴사 준비의 런웨이에 흘러간다 — 틀린 줄도 모르고 그 숫자를 보게 된다.
 *
 * 다른 기여자들과 성격이 다르다. 만기·가이드·목표는 **밖에서 정해진 날**이 다가오는 것을
 * 알리지만, 여기는 **사용자가 할 일이 남았다**고 말한다. 그래서 날짜가 아니라 상태를 본다.
 *
 * ## 언제 알리는가
 *
 * 달이 바뀐 뒤 **[NOTICE_WINDOW_DAYS]일 안쪽**에만. 둘 다 이유가 있다.
 *
 * - 달이 끝나야 쓴 돈이 확정되고 카드 명세서도 그때 나온다 — 그 전에 알리면 적을 수가 없다
 * - 창을 닫지 않으면 안 적은 사람에게 **한 달 내내 같은 줄이 간다.** 그러면 다이제스트를
 *   열지 않게 되고, 만기처럼 정말 놓치면 손해인 것까지 함께 묻힌다
 *
 * 그 기간에 안 적었으면 조용히 넘어간다. 화면에는 언제든 적을 수 있다.
 */
@Component
class SpendingNotificationContributor(
    private val spendingRecordRepository: SpendingRecordRepository,
) : NotificationContributor {
    override val moduleIds: Set<ModuleId> = setOf(SpendingModule.MODULE_ID)

    override fun noticesFor(
        userId: UserId,
        today: LocalDate,
    ): List<Notice> {
        if (today.dayOfMonth > NOTICE_WINDOW_DAYS) return emptyList()

        val lastMonth = SpendingRecord.normalizeMonth(today).minusMonths(1)
        if (spendingRecordRepository.findByUserIdAndMonth(userId, lastMonth) != null) return emptyList()

        return listOf(
            Notice(
                moduleId = SpendingModule.MODULE_ID,
                /*
                 * 걸린 날은 **그 달의 마지막 날**이다. 오늘로 두면 다이제스트가 가까운 것부터
                 * 늘어놓을 때 만기 같은 것들과 섞여 맨 위에 올라온다 — 재촉의 세기가 다르다.
                 */
                on = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()),
                title = "${lastMonth.monthValue}월에 쓴 돈을 아직 안 적었어요",
                detail = "카드 명세서 합계를 옮겨 적으면 평소 얼마 쓰는지 다시 맞아요",
            ),
        )
    }

    private companion object {
        /**
         * 달이 바뀐 뒤 며칠까지 알릴지.
         *
         * 명세서가 나오기까지 며칠 걸리고 주말이 끼기도 해서 하루로는 짧다. 반대로 길면
         * 안 적는 사람에게 매일 같은 줄이 가므로, 한 주가 그 사이다.
         */
        const val NOTICE_WINDOW_DAYS = 7
    }
}
