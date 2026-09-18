package io.aetera.model.notification

import io.aetera.model.user.UserId
import io.aetera.shared.error.CoreException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

/**
 * 메일이 나갈지 말지를 혼자 정하는 로직이라 경계를 못 박는다.
 *
 * 되돌릴 수 없는 동작이다 — 한 칸이 뒤집히면 알림이 통째로 안 가거나 같은 메일이 두 번 간다.
 */
class NotificationPreferenceTest :
    DescribeSpec({
        val today = LocalDate.of(2026, 9, 19)

        fun preference(
            enabled: Boolean = true,
            sendHour: Int = 8,
            lastSentOn: LocalDate? = null,
        ) = NotificationPreference.reconstitute(
            id = NotificationPreferenceId.next(),
            userId = UserId.next(),
            enabled = enabled,
            sendHour = sendHour,
            lastSentOn = lastSentOn,
        )

        describe("보낼 때인가") {
            it("정한 시각이 되면 보낸다") {
                preference(sendHour = 8).shouldSend(today, 8) shouldBe true
            }

            it("아직 이르면 안 보낸다") {
                preference(sendHour = 8).shouldSend(today, 7) shouldBe false
            }

            /*
             * 시각이 지났어도 보낸다. 딱 그 시각에만 보내면 서버가 잠깐 멈췄다 살아난 날은
             * 그날 알림이 통째로 사라진다.
             */
            it("시각이 지났으면 보낸다 — 멈췄다 살아난 날을 위해") {
                preference(sendHour = 8).shouldSend(today, 23) shouldBe true
            }

            it("끈 사람에게는 안 보낸다") {
                preference(enabled = false).shouldSend(today, 23) shouldBe false
            }
        }

        describe("하루 한 통") {
            it("오늘 이미 보냈으면 또 보내지 않는다") {
                preference(lastSentOn = today).shouldSend(today, 9) shouldBe false
            }

            it("어제 보낸 것은 오늘을 막지 않는다") {
                preference(lastSentOn = today.minusDays(1)).shouldSend(today, 9) shouldBe true
            }

            it("표시하면 그날은 닫힌다") {
                val preference = preference()
                preference.shouldSend(today, 9) shouldBe true
                preference.markSent(today)
                preference.shouldSend(today, 9) shouldBe false
                preference.shouldSend(today.plusDays(1), 9) shouldBe true
            }
        }

        describe("받을 시각") {
            it("0시와 23시는 된다") {
                NotificationPreference
                    .create(NotificationPreferenceId.next(), UserId.next(), sendHour = 0)
                    .sendHour shouldBe 0
                NotificationPreference
                    .create(NotificationPreferenceId.next(), UserId.next(), sendHour = 23)
                    .sendHour shouldBe 23
            }

            it("24시는 막는다") {
                shouldThrow<CoreException> {
                    NotificationPreference.create(NotificationPreferenceId.next(), UserId.next(), sendHour = 24)
                }.message shouldBe "받을 시각은 0시부터 23시 사이여야 합니다. 입력: 24"
            }

            it("음수도 막는다") {
                shouldThrow<CoreException> {
                    NotificationPreference.create(NotificationPreferenceId.next(), UserId.next(), sendHour = -1)
                }
            }

            it("바꿀 때도 본다") {
                shouldThrow<CoreException> { preference().change(enabled = true, sendHour = 99) }
            }
        }
    })
