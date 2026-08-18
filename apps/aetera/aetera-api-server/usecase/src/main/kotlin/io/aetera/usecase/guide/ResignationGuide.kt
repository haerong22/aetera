package io.aetera.usecase.guide

import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideLink
import io.aetera.model.guide.GuidePhase
import io.aetera.model.guide.GuideTask
import io.aetera.model.guide.GuideTaskKey
import io.aetera.model.guide.GuideTemplate

/**
 * 퇴사 준비 가이드의 콘텐츠.
 *
 * `dueOffsetDays` 는 퇴사 예정일(D-day) 기준 상대 일수다. 사용자가 자기 퇴사일을 넣으면
 * 엔진이 이걸 내 달력의 실제 날짜로 바꾼다 — 콘텐츠에는 절대 날짜가 한 개도 없다.
 *
 * `required` 는 "놓치면 돈이나 권리를 잃는가"로만 나눈다. 참고용까지 필수로 두면
 * 진행률이 영영 100% 가 되지 않아 지표로 쓸 수 없다.
 *
 * **할 일 키는 배포 후 바꾸지 않는다.** 사용자의 체크 상태가 이 키로 저장된다.
 */
private object Links {
    val EI = GuideLink("고용보험", "https://www.ei.go.kr")
    val NHIS = GuideLink("국민건강보험공단", "https://www.nhis.or.kr")
    val NPS = GuideLink("국민연금공단", "https://www.nps.or.kr")
    val MOEL = GuideLink("고용노동부", "https://www.moel.go.kr")
    val HOMETAX = GuideLink("국세청 홈택스", "https://www.hometax.go.kr")
    val FOUR_INSURE = GuideLink("4대사회보험 정보연계센터", "https://www.4insure.or.kr")
}

private fun task(
    key: String,
    title: String,
    description: String,
    dueOffsetDays: Int,
    required: Boolean = true,
    link: GuideLink? = null,
) = GuideTask(GuideTaskKey(key), title, description, dueOffsetDays, required, link)

internal val RESIGNATION_GUIDE: GuideTemplate =
    GuideTemplate(
        id = GuideId("resignation"),
        title = "퇴사 준비",
        summary = "퇴사일을 정하면 무엇을 어떤 순서로 준비해야 하는지 알려드려요.",
        anchorLabel = "퇴사 예정일",
        disclaimer =
            "회사 규정과 개인 상황에 따라 달라질 수 있어요. 금액·기한이 걸린 항목은 " +
                "인사팀이나 각 기관에 한 번 더 확인하시는 걸 권해요.",
        phases =
            listOf(
                GuidePhase(
                    key = "prepare",
                    title = "결심 굳히기",
                    summary = "통보하기 전에 끝내 두면 좋은 것들. 재직 중이어야 유리한 일이 생각보다 많아요.",
                    tasks =
                        listOf(
                            task(
                                key = "finance-runway",
                                title = "생활비가 몇 달 버티는지 계산하기",
                                description =
                                    "다음 수입까지의 공백을 숫자로 확인해요. 고정지출(월세·보험료·대출 상환)만 따로 더해 보면 " +
                                        "실제로 버틸 수 있는 개월 수가 나와요.",
                                dueOffsetDays = -60,
                            ),
                            task(
                                key = "next-plan",
                                title = "다음 계획 구체화하기",
                                description = "이직, 휴식, 공부, 창업 중 무엇인지에 따라 준비할 것도 퇴사 시점도 달라져요.",
                                dueOffsetDays = -55,
                                required = false,
                            ),
                            task(
                                key = "company-rules",
                                title = "취업규칙에서 퇴직 관련 조항 확인하기",
                                description =
                                    "퇴직 통보 기간, 퇴직금 산정 방식, 교육비·사이닝보너스 반환 조건을 미리 봐 두세요. " +
                                        "약정 기간을 안 채우면 돌려줘야 하는 돈이 있는 경우가 있어요.",
                                dueOffsetDays = -50,
                            ),
                            task(
                                key = "annual-leave-check",
                                title = "잔여 연차 일수 확인하기",
                                description =
                                    "남은 연차는 퇴사 전에 쓰거나 미사용 수당으로 정산받아요. " +
                                        "일수를 먼저 알아야 퇴사일을 언제로 잡을지 정할 수 있어요.",
                                dueOffsetDays = -45,
                            ),
                            task(
                                key = "loan-and-credit",
                                title = "대출·카드 등 재직 중에 처리할 금융 업무 끝내기",
                                description =
                                    "전세자금대출 연장, 신용대출, 카드 발급은 재직증명서와 소득증빙이 필요해요. " +
                                        "퇴사 후에는 조건이 크게 나빠지거나 아예 안 되는 경우가 많으니 지금 처리하세요.",
                                dueOffsetDays = -40,
                            ),
                            task(
                                key = "welfare-benefits",
                                title = "회사 복지 남은 것 쓰기",
                                description = "건강검진, 복지포인트, 교육비 지원, 단체보험 항목처럼 재직자만 쓸 수 있는 것들을 정리해서 소진해요.",
                                dueOffsetDays = -35,
                                required = false,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "notice",
                    title = "통보하기",
                    summary = "순서가 중요해요. 소문이 먼저 도는 것과 직접 말하는 것은 남는 인상이 다릅니다.",
                    tasks =
                        listOf(
                            task(
                                key = "notice-verbal",
                                title = "직속 상사에게 먼저 구두로 알리기",
                                description =
                                    "사직서보다 대화가 먼저예요. 이유는 담백하게, 확정된 사실로 전하면 " +
                                        "설득 국면이 아니라 인수인계 논의로 넘어갑니다.",
                                dueOffsetDays = -30,
                            ),
                            task(
                                key = "resignation-letter",
                                title = "사직서 제출하기",
                                description = "제출일과 희망 퇴사일을 명확히 적고, 제출한 사본은 따로 보관해 두세요.",
                                dueOffsetDays = -28,
                            ),
                            task(
                                key = "confirm-last-day",
                                title = "퇴사일 확정하고 합의 남기기",
                                description =
                                    "연차 소진까지 포함한 마지막 근무일과 퇴사일을 회사와 맞춰요. " +
                                        "메일 등 기록으로 남기면 나중에 정산 다툼이 줄어요.",
                                dueOffsetDays = -25,
                            ),
                            task(
                                key = "handover-doc",
                                title = "인수인계 문서 작성 시작하기",
                                description =
                                    "담당 업무, 반복 일정, 계정·권한, 외부 담당자 연락처, 진행 중인 건의 현재 상태를 적어요. " +
                                        "\"나만 아는 것\"을 먼저 적는 게 요령이에요.",
                                dueOffsetDays = -21,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "wrapup",
                    title = "마무리하기",
                    summary = "마지막 2주. 회사에 돌려줄 것과 내가 챙길 것을 분리해서 정리해요.",
                    tasks =
                        listOf(
                            task(
                                key = "handover-run",
                                title = "인수인계 실행하고 후임 교육하기",
                                description = "문서만 넘기지 말고 한 사이클을 같이 돌려 보면 퇴사 후 연락 오는 일이 확 줄어요.",
                                dueOffsetDays = -14,
                            ),
                            task(
                                key = "leave-plan",
                                title = "잔여 연차 소진 일정 잡기",
                                description = "남은 연차를 언제 쓸지 확정해요. 다 못 쓰면 미사용 연차수당으로 정산받게 됩니다.",
                                dueOffsetDays = -14,
                            ),
                            task(
                                key = "personal-files",
                                title = "개인 파일과 연락처 백업하기",
                                description =
                                    "회사 자산이 아닌 내 자료(포트폴리오로 쓸 수 있는 산출물, 동료 연락처)만 골라 옮겨요. " +
                                        "회사 기밀·고객 정보는 반출하면 안 됩니다.",
                                dueOffsetDays = -5,
                                required = false,
                            ),
                            task(
                                key = "return-assets",
                                title = "반납할 회사 자산 목록 만들기",
                                description = "노트북, 모니터, 출입카드, 법인카드, 사원증, 주차권, 도서. 반납 확인을 받아 두면 나중에 깔끔해요.",
                                dueOffsetDays = -2,
                            ),
                            task(
                                key = "account-cleanup",
                                title = "사내 계정에서 개인 정보 지우기",
                                description = "업무용 브라우저·메신저에 남은 개인 로그인, 저장된 비밀번호, 개인 결제수단을 지워요.",
                                dueOffsetDays = -1,
                                required = false,
                            ),
                            task(
                                key = "last-day-docs",
                                title = "퇴사 당일에 받을 서류 챙기기",
                                description =
                                    "경력증명서는 미리 신청해 두면 당일에 받을 수 있어요. " +
                                        "퇴사 후에는 담당자가 바뀌어 발급이 늦어지는 일이 흔합니다.",
                                dueOffsetDays = 0,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "after",
                    title = "퇴사 직후",
                    summary = "여기가 진짜예요. 기한을 놓치면 돌려받을 수 없는 것들이 몰려 있어요.",
                    tasks =
                        listOf(
                            task(
                                key = "insurance-loss",
                                title = "4대보험 상실 신고가 처리됐는지 확인하기",
                                description =
                                    "회사가 신고하는 절차지만, 늦어지면 건강보험 전환과 실업급여 신청이 함께 밀려요. " +
                                        "직접 조회해서 처리 여부를 확인하세요.",
                                dueOffsetDays = 7,
                                link = Links.FOUR_INSURE,
                            ),
                            task(
                                key = "employment-cert",
                                title = "이직확인서 처리 확인하기",
                                description =
                                    "실업급여를 받으려면 회사가 제출한 이직확인서가 처리돼 있어야 해요. " +
                                        "이직 사유가 사실과 다르게 적히면 수급이 막힐 수 있으니 내용까지 확인하세요.",
                                dueOffsetDays = 7,
                                link = Links.EI,
                            ),
                            task(
                                key = "health-insurance",
                                title = "건강보험 전환 방식 정하기",
                                description =
                                    "지역가입자 전환, 피부양자 등재, 임의계속가입 중에 고릅니다. 임의계속가입은 조건을 갖추면 " +
                                        "직장 다닐 때 수준의 보험료를 일정 기간 유지할 수 있는데, 신청 기한이 짧으니 먼저 문의해 보세요.",
                                dueOffsetDays = 10,
                                link = Links.NHIS,
                            ),
                            task(
                                key = "pension-exception",
                                title = "국민연금 납부예외 신청 검토하기",
                                description =
                                    "소득이 없는 기간에는 납부예외를 신청할 수 있어요. 그동안은 가입 기간에 안 들어가니 " +
                                        "나중에 추납할지도 같이 생각해 두세요.",
                                dueOffsetDays = 10,
                                required = false,
                                link = Links.NPS,
                            ),
                            task(
                                key = "unemployment-benefit",
                                title = "실업급여 수급자격 확인하고 신청하기",
                                description =
                                    "고용보험 가입 기간과 이직 사유 요건을 채워야 해요. 수급 기간에 한도가 있어서 " +
                                        "신청이 늦어지면 그만큼 못 받게 되니 자격이 된다면 바로 진행하세요.",
                                dueOffsetDays = 14,
                                link = Links.EI,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "settle",
                    title = "정산과 서류",
                    summary = "받을 돈을 받았는지, 다음에 필요한 서류를 챙겼는지 확인해요.",
                    tasks =
                        listOf(
                            task(
                                key = "severance-pay",
                                title = "퇴직금 수령 확인하기",
                                description =
                                    "퇴직금은 퇴직일로부터 정해진 기한 안에 지급하는 것이 원칙이에요(당사자 합의로 연장 가능). " +
                                        "금액이 예상과 다르면 산정 기준이 된 평균임금부터 확인해 보세요.",
                                dueOffsetDays = 14,
                                link = Links.MOEL,
                            ),
                            task(
                                key = "final-payroll",
                                title = "마지막 급여와 연차수당 정산 확인하기",
                                description = "일할 계산된 마지막 달 급여, 미사용 연차수당, 상여금 정산분이 다 들어왔는지 급여명세서로 대조해요.",
                                dueOffsetDays = 14,
                            ),
                            task(
                                key = "irp-account",
                                title = "IRP 계좌 확인하기",
                                description = "퇴직연금에 가입돼 있었다면 퇴직급여가 IRP 계좌로 들어와요. 바로 찾을지 굴릴지에 따라 세금이 달라집니다.",
                                dueOffsetDays = 21,
                                required = false,
                            ),
                            task(
                                key = "withholding-receipt",
                                title = "원천징수영수증 발급받기",
                                description =
                                    "근로소득·퇴직소득 원천징수영수증은 이직한 회사의 연말정산과 종합소득세 신고에 필요해요. " +
                                        "홈택스에서도 조회되지만 반영이 늦을 수 있으니 회사에서 받아 두면 확실해요.",
                                dueOffsetDays = 30,
                                link = Links.HOMETAX,
                            ),
                            task(
                                key = "career-cert",
                                title = "경력증명서 받아 두기",
                                description = "재직 기간과 직무가 적힌 증명서는 다음 회사와 각종 자격 신청에 쓰여요. 시간이 지날수록 발급이 번거로워집니다.",
                                dueOffsetDays = 30,
                                required = false,
                            ),
                            task(
                                key = "tax-docs",
                                title = "연말정산 대비 서류 모아 두기",
                                description = "퇴직한 해의 소득은 다음 해에 직접 신고해야 할 수 있어요. 영수증과 증빙을 한곳에 모아 두세요.",
                                dueOffsetDays = 45,
                                required = false,
                            ),
                        ),
                ),
            ),
    )
