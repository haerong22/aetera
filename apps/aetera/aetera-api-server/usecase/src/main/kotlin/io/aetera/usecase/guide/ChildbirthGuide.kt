package io.aetera.usecase.guide

import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideLink
import io.aetera.model.guide.GuidePhase
import io.aetera.model.guide.GuideTemplate

/**
 * 출산 준비 가이드의 콘텐츠.
 *
 * **기한이 법으로 정해진 것이 가장 많은 가이드다.** 출생신고는 한 달을 넘기면 과태료가 붙고,
 * 육아휴직은 시작 30일 전까지 신청해야 하고, 수당은 신청한 날부터 센다. 놓쳐서 잃는 것이
 * 돈과 권리라 그런 항목만 필수로 두었다 — 입원 가방 싸기는 중요하지만 놓쳐도 되돌릴 수 있다.
 *
 * **금액과 지원 기간은 적지 않는다.** 제도가 해마다 바뀌어서 적는 순간 틀린 숫자가 되고,
 * 숫자가 틀린 안내는 없는 것보다 나쁘다. 대신 **그런 제도가 있다는 것과 언제까지 해야 하는지**를
 * 적고 공식 창구로 보낸다. 기한은 법에 있는 것만 적었다.
 *
 * 연말정산과 겹치는 것은 넣지 않았다 — 부양가족 요건과 의료비 공제는 그쪽 가이드의 몫이다.
 * 여기서는 "아이가 생기면 연말정산이 달라진다"는 것만 알리고 넘긴다.
 */
internal val CHILDBIRTH_GUIDE: GuideTemplate =
    GuideTemplate(
        id = GuideId("childbirth"),
        title = "출산 준비",
        summary = "출산 예정일을 정하면 지원 신청부터 휴가, 출생신고, 육아휴직까지 기한별로 짚어드려요.",
        anchorLabel = "출산 예정일",
        disclaimer =
            "지원 제도와 금액은 해마다 바뀌고 지자체마다 다릅니다. 여기서는 기한만 안내하니 " +
                "실제 금액과 요건은 공식 창구에서 확인하세요. 회사 규정에 따라 휴가 절차가 다를 수 있어요.",
        phases =
            listOf(
                GuidePhase(
                    key = "confirm",
                    title = "임신을 확인하고 나면",
                    summary = "지원은 대개 신청한 날부터 셉니다. 일찍 해 두면 그만큼 더 받아요.",
                    tasks =
                        listOf(
                            task(
                                key = "health-center",
                                title = "보건소에 임신 등록하기",
                                description =
                                    "엽산·철분제, 기형아 검사, 지자체 지원이 대개 여기서 시작돼요. " +
                                        "사는 곳에 따라 주는 것이 달라서 무엇이 있는지 한 번 물어보는 것이 빠릅니다.",
                                dueOffsetDays = -250,
                            ),
                            task(
                                key = "voucher",
                                title = "임신·출산 진료비 지원 신청하기",
                                description =
                                    "국민행복카드로 받아 진료비에 씁니다. 출산 후에도 일정 기간 쓸 수 있으니 " +
                                        "남았다고 서둘러 쓰지 않아도 돼요. 신청 전 진료비는 소급되지 않습니다.",
                                dueOffsetDays = -240,
                                link = GuideLink("국민건강보험공단", "https://www.nhis.or.kr"),
                            ),
                            task(
                                key = "hospital",
                                title = "출산할 병원 정하기",
                                description =
                                    "분만 방식, 야간·주말 분만 가능 여부, 신생아 집중치료실이 있는지를 봐요. " +
                                        "집에서 가는 시간도 중요합니다 — 진통이 시작되면 그 시간이 전부예요.",
                                dueOffsetDays = -220,
                            ),
                            task(
                                key = "insurance-check",
                                title = "가입한 보험의 임신·출산 보장 확인하기",
                                description =
                                    "임신을 알고 난 뒤에 드는 보험은 대개 이번 임신을 보장하지 않아요. " +
                                        "이미 가입한 것에서 무엇이 되는지만 확인해 두세요.",
                                dueOffsetDays = -200,
                                required = false,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "work",
                    title = "회사와 맞추기",
                    summary =
                        "휴가와 육아휴직은 신청 기한이 법에 있어요. 미리 말해 두면 회사도 " +
                            "대체 인력을 구할 시간이 생겨 서로 편합니다.",
                    tasks =
                        listOf(
                            task(
                                key = "notify-company",
                                title = "회사에 알리고 근로시간 단축 알아보기",
                                description =
                                    "임신 초기와 후기에는 하루 2시간 단축을 쓸 수 있어요. 급여는 깎이지 않습니다. " +
                                        "위험한 업무에서 빼 달라고 요청할 권리도 있어요.",
                                dueOffsetDays = -180,
                                link = GuideLink("고용노동부", "https://www.moel.go.kr"),
                            ),
                            task(
                                key = "leave-plan",
                                title = "출산전후휴가 일정 짜기",
                                description =
                                    "90일이고 출산 후에 45일 이상 남겨야 합니다. 쌍둥이는 120일이에요. " +
                                        "언제부터 들어갈지는 몸 상태와 업무를 보고 정하되, 예정일이 밀릴 수 있다는 걸 감안하세요.",
                                dueOffsetDays = -150,
                            ),
                            task(
                                key = "parental-leave-plan",
                                title = "육아휴직을 누가 언제 쓸지 정하기",
                                description =
                                    "둘 다 쓸 수 있고 나눠 쓸 수도 있어요. 누가 먼저 쓰는지에 따라 급여가 달라지는 " +
                                        "제도가 있으니 계획 단계에서 함께 알아보세요. 신청서는 나중에 냅니다.",
                                dueOffsetDays = -120,
                                required = false,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "before",
                    title = "낳기 전에",
                    summary = "예정일은 예정일일 뿐이에요. 2주 앞뒤로 움직인다고 보고 준비하세요.",
                    tasks =
                        listOf(
                            task(
                                key = "baby-stuff",
                                title = "꼭 필요한 것만 먼저 사기",
                                description =
                                    "처음 한 달에 실제로 쓰는 것은 생각보다 적어요. 미리 다 사 두면 " +
                                        "안 쓰는 것이 쌓이고, 아이마다 맞는 것이 달라서 바꾸게 됩니다.",
                                dueOffsetDays = -100,
                                required = false,
                            ),
                            task(
                                key = "birth-plan",
                                title = "출산 방식과 입원 준비 정하기",
                                description =
                                    "자연분만·제왕절개, 무통 여부, 입원 기간을 병원과 상의해 두세요. " +
                                        "당일에 정하기 어려운 것들이라 미리 이야기해 두면 덜 당황합니다.",
                                dueOffsetDays = -60,
                                required = false,
                            ),
                            task(
                                key = "leave-start",
                                title = "출산전후휴가 들어가기",
                                description =
                                    "회사에 신청하고 인수인계를 마무리해요. 예정일이 밀리면 휴가도 함께 밀리니 " +
                                        "복귀일을 못 박아 두지 말고 범위로 말해 두세요.",
                                dueOffsetDays = -45,
                            ),
                            task(
                                key = "hospital-bag",
                                title = "입원 가방 싸 두기",
                                description =
                                    "신분증, 산모수첩, 세면도구, 수유용품, 아이 옷. 진통이 시작되면 " +
                                        "챙길 정신이 없어서 미리 싸 두는 것 자체가 준비입니다.",
                                dueOffsetDays = -21,
                                required = false,
                            ),
                            task(
                                key = "contact-plan",
                                title = "진통이 오면 누가 무엇을 할지 정하기",
                                description =
                                    "누구에게 연락하고, 누가 운전하고, 밤이면 어떻게 할지. 반려동물이나 " +
                                        "다른 아이가 있으면 그쪽도 정해 두세요.",
                                dueOffsetDays = -14,
                                required = false,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "report",
                    title = "낳은 뒤 한 달 안에",
                    summary =
                        "여기가 기한이 가장 빡빡해요. 출생신고는 한 달을 넘기면 과태료가 붙고, " +
                            "수당은 신청한 날부터 셉니다.",
                    tasks =
                        listOf(
                            task(
                                key = "birth-report",
                                title = "출생신고 하기",
                                description =
                                    "태어난 날부터 한 달 안에 해야 하고 늦으면 과태료가 붙어요. " +
                                        "이름을 정해 가야 하고, 한 번 신고하면 바꾸기가 매우 어렵습니다.",
                                dueOffsetDays = 7,
                                link = GuideLink("정부24", "https://www.gov.kr"),
                            ),
                            task(
                                key = "health-insurance",
                                title = "아이를 건강보험에 올리기",
                                description =
                                    "피부양자로 등록해야 아이 진료비가 보험 적용을 받아요. " +
                                        "출생신고와 함께 처리되는 경우도 있지만 확인하는 편이 안전합니다.",
                                dueOffsetDays = 10,
                                link = GuideLink("국민건강보험공단", "https://www.nhis.or.kr"),
                            ),
                            task(
                                key = "benefits",
                                title = "아동수당·부모급여·첫만남이용권 신청하기",
                                description =
                                    "출생신고와 같은 자리에서 함께 신청할 수 있어요. " +
                                        "신청한 날부터 세는 것들이라 미루면 그만큼 못 받습니다. 금액과 요건은 해마다 바뀌어요.",
                                dueOffsetDays = 14,
                                link = GuideLink("복지로", "https://www.bokjiro.go.kr"),
                            ),
                            task(
                                key = "postpartum-care",
                                title = "산후 관리와 예방접종 일정 확인하기",
                                description =
                                    "산후조리 지원이 지자체마다 있어요. 아이 예방접종은 시기가 정해져 있고 " +
                                        "대부분 무료입니다. 일정 모듈에 넣어 두면 놓치지 않아요.",
                                dueOffsetDays = 30,
                                required = false,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "after",
                    title = "돌아갈 준비",
                    summary =
                        "휴가가 끝나기 전에 움직여야 하는 것들이에요. 육아휴직은 시작 30일 전까지 " +
                            "신청해야 하므로 휴가 중에 챙겨야 합니다.",
                    tasks =
                        listOf(
                            task(
                                key = "parental-leave-apply",
                                title = "육아휴직 신청서 내기",
                                description =
                                    "시작하려는 날부터 30일 전까지 회사에 내야 해요. 출산전후휴가가 끝나는 날에 " +
                                        "이어서 쓸 거면 휴가 중에 내야 기한을 맞춥니다.",
                                dueOffsetDays = 45,
                                link = GuideLink("고용노동부", "https://www.moel.go.kr"),
                            ),
                            task(
                                key = "leave-benefit",
                                title = "육아휴직 급여 신청하기",
                                description =
                                    "회사가 주는 것이 아니라 고용보험에서 받습니다. 휴직 시작 후에 신청하고, " +
                                        "신청하지 않으면 나오지 않아요.",
                                dueOffsetDays = 60,
                                link = GuideLink("고용보험", "https://www.ei.go.kr"),
                            ),
                            task(
                                key = "childcare",
                                title = "어린이집·돌봄 알아보기",
                                description =
                                    "대기가 긴 곳이 많아 복귀 시점보다 훨씬 먼저 넣어 둬야 해요. " +
                                        "조부모 돌봄이나 아이돌봄서비스도 함께 알아보세요.",
                                dueOffsetDays = 120,
                                required = false,
                            ),
                            task(
                                key = "return-plan",
                                title = "복귀 조건 확인하기",
                                description =
                                    "같은 일자리나 같은 수준의 일자리로 돌아갈 권리가 있어요. " +
                                        "복귀 후 근로시간 단축도 쓸 수 있으니 미리 회사와 이야기해 두세요.",
                                dueOffsetDays = 180,
                                required = false,
                            ),
                            task(
                                key = "tax-change",
                                title = "연말정산에서 달라지는 것 챙기기",
                                description =
                                    "아이가 부양가족으로 들어가고 의료비도 늘어나요. 어떤 서류가 필요한지는 " +
                                        "연말정산 준비 모듈이 항목별로 짚어드려요.",
                                dueOffsetDays = 300,
                                required = false,
                            ),
                        ),
                ),
            ),
    )
