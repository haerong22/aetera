package io.aetera.usecase.guide

import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideLink
import io.aetera.model.guide.GuidePhase
import io.aetera.model.guide.GuideTemplate

/**
 * 결혼 준비 가이드의 콘텐츠.
 *
 * 다른 가이드와 다른 점이 둘 있다.
 *
 * **기간이 가장 길다.** 식장을 1년 전에 잡는 일이 흔해서 기준일 1년 전부터 시작한다. 그래서
 * 앞 단계의 할 일이 성겁게 놓여 있다 — 촘촘하게 깔면 아직 아무것도 할 수 없는 시기에
 * 지난 항목만 쌓인다.
 *
 * **둘이 하는 일이다.** 다른 가이드는 혼자 결정하면 되지만 여기는 상대와 맞춰야 하는 것이
 * 대부분이고, 돈 이야기가 섞여 있다. 그래서 "정하기" 항목에 **무엇을 합의해야 하는지**까지 적었다 —
 * 체크박스만 있으면 "예산 정하기"를 각자 다르게 이해한 채로 체크한다.
 *
 * **이사 준비와 겹치는 것은 넣지 않았다.** 결혼하면서 이사하는 조합이 가장 흔한데, 둘을 켠
 * 사람이 "등기부등본 확인"을 두 번 보면 어느 쪽을 체크해야 할지 알 수 없다. 그래서 신혼집 단계는
 * **둘이 합의할 것**(예산 배분, 계약 명의)만 다루고, 계약 점검과 이사 실무는 그쪽으로 넘긴다.
 *
 * 혼수·예단·예복처럼 **지역과 집안에 따라 아예 안 하는 것들**은 `required = false` 다.
 * 필수로 두면 안 하기로 한 사람이 영영 100% 에 닿지 못한다.
 *
 * 돈 항목은 금액을 적지 않는다. 식장·지역·인원에 따라 열 배씩 차이 나서, 적는 순간
 * 누군가에게는 틀린 숫자가 된다. 대신 **무엇을 세어야 하는지**를 적는다.
 */
internal val WEDDING_GUIDE: GuideTemplate =
    GuideTemplate(
        id = GuideId("wedding"),
        title = "결혼 준비",
        summary = "결혼식 날짜를 정하면 식장 예약부터 예산, 혼인신고, 신혼집까지 순서대로 짚어드려요.",
        anchorLabel = "결혼식 날짜",
        disclaimer =
            "지역과 집안의 관습에 따라 하는 것과 안 하는 것이 크게 달라요. 안 하기로 한 항목은 " +
                "건너뛰셔도 진행률에 영향이 없게 해 두었어요. 계약과 환불 조건은 반드시 문서로 확인하세요.",
        phases =
            listOf(
                GuidePhase(
                    key = "decide",
                    title = "둘이 정하기",
                    summary =
                        "식장을 보러 다니기 전에 정해야 할 것들이에요. 순서를 바꾸면 " +
                            "보고 온 곳에 마음이 끌려 예산이 뒤에서 따라오게 됩니다.",
                    tasks =
                        listOf(
                            task(
                                key = "budget-total",
                                title = "총예산과 각자 낼 몫 정하기",
                                description =
                                    "총액만 정하면 나중에 다툽니다. 각자 얼마를 내는지, 양가 지원이 있다면 " +
                                        "그 돈의 성격이 무엇인지(보태는 것인지 빌리는 것인지)까지 말로 꺼내 두세요. " +
                                        "자산 모듈에 지금 가진 돈을 적어 두면 무리 없는 선이 보여요.",
                                dueOffsetDays = -365,
                            ),
                            task(
                                key = "scale",
                                title = "규모와 형식 정하기",
                                description =
                                    "하객 수가 식장·식대·청첩장까지 전부를 끌고 갑니다. 양가가 부르고 싶은 사람 수를 " +
                                        "먼저 각각 세어 보면 현실적인 숫자가 나와요. 스몰웨딩·노웨딩도 여기서 결정합니다.",
                                dueOffsetDays = -360,
                            ),
                            task(
                                key = "skip-list",
                                title = "안 할 것 먼저 정하기",
                                description =
                                    "예단·혼수·함·폐백 중 하지 않을 것을 미리 합의해 두세요. 진행하다 빼는 것이 " +
                                        "처음부터 안 하는 것보다 훨씬 어렵고, 양가 감정이 상하는 자리입니다.",
                                dueOffsetDays = -355,
                                required = false,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "book",
                    title = "예약하기",
                    summary = "날짜를 붙잡는 단계예요. 인기 있는 날은 1년 전에 차므로 가장 먼저 움직입니다.",
                    tasks =
                        listOf(
                            task(
                                key = "venue",
                                title = "식장 계약하기",
                                description =
                                    "보증 인원과 식대 단가, 대관료에 무엇이 포함되는지를 봐요. 보증 인원은 " +
                                        "안 와도 내야 하는 숫자라 하객 수를 낙관적으로 잡으면 그만큼 손해입니다. " +
                                        "위약금 조항과 날짜 변경 가능 여부도 계약서에서 확인하세요.",
                                dueOffsetDays = -330,
                            ),
                            task(
                                key = "studio-dress-makeup",
                                title = "스튜디오·드레스·메이크업 알아보기",
                                description =
                                    "묶어 계약하면 싸지만 각각의 선택권이 줄어요. 피팅 횟수, 드레스 추가 비용, " +
                                        "원본 사진 제공 여부처럼 나중에 돈이 붙는 항목을 계약 전에 물어보세요.",
                                dueOffsetDays = -300,
                                required = false,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "home",
                    title = "신혼집 준비하기",
                    summary =
                        "식 준비와 따로 돌아가는 일이에요. 대출이 걸리면 가장 오래 걸리니 " +
                            "식장만 잡고 미뤄 두면 막판에 몰립니다.",
                    tasks =
                        listOf(
                            task(
                                key = "house-budget",
                                title = "총예산에서 집에 쓸 몫 떼어 내기",
                                description =
                                    "식과 집은 같은 주머니에서 나가요. 어느 쪽에 얼마를 쓸지 둘이 정해 두지 않으면 " +
                                        "먼저 계약하는 쪽이 남은 예산을 다 끌고 갑니다. " +
                                        "집에 실제로 드는 돈(중개수수료·이사비까지)은 이사 준비 모듈이 항목별로 짚어드려요.",
                                dueOffsetDays = -240,
                            ),
                            task(
                                key = "loan",
                                title = "대출 알아보기",
                                description =
                                    "신혼부부 전용 상품은 소득·혼인 기간 요건이 있어요. 혼인신고 전후로 조건이 " +
                                        "달라지는 상품도 있으니 신고 시점을 정하기 전에 확인하세요.",
                                dueOffsetDays = -200,
                                required = false,
                                link = GuideLink("주택도시기금", "https://nhuf.molit.go.kr"),
                            ),
                            task(
                                key = "house-contract",
                                title = "계약 명의와 지분 정하기",
                                description =
                                    "누구 이름으로 계약하고 돈을 얼마씩 냈는지를 계약 전에 정해 두세요. " +
                                        "나중에 대출·청약·세금에서 전부 이 명의를 따라갑니다. " +
                                        "등기부등본 확인과 보증보험 같은 계약 자체의 점검은 이사 준비 모듈이 짚어드려요.",
                                dueOffsetDays = -150,
                            ),
                            task(
                                key = "move-in",
                                title = "입주와 이사 날짜 맞추기",
                                description =
                                    "식 전에 들어가는지 후에 들어가는지로 짐 옮기는 순서가 전부 달라져요. " +
                                        "이사 준비 모듈을 켜면 전입신고·확정일자까지 날짜별로 짚어드려요.",
                                dueOffsetDays = -130,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "invite",
                    title = "알리고 마무리하기",
                    summary = "식이 가까워지면 할 일이 몰려요. 미리 할 수 있는 것은 당겨 두는 편이 낫습니다.",
                    tasks =
                        listOf(
                            task(
                                key = "officiant",
                                title = "사회·축가·주례 부탁하기",
                                description =
                                    "부탁받는 사람도 준비할 시간이 필요해요. 늦게 말하면 거절하기 어려운 부탁이 됩니다. " +
                                        "주례 없이 양가 부모님 말씀으로 대신하는 식도 흔해요.",
                                dueOffsetDays = -120,
                                required = false,
                            ),
                            task(
                                key = "guest-list",
                                title = "하객 명단 확정하기",
                                description =
                                    "양가 각자 적어서 합쳐요. 식대가 사람 수로 붙으니 이 명단이 예산의 마지막 변수입니다. " +
                                        "보증 인원과 견줘 보고 모자라면 식장과 미리 조율하세요.",
                                dueOffsetDays = -60,
                            ),
                            task(
                                key = "honeymoon",
                                title = "신혼여행 예약하고 서류 확인하기",
                                description =
                                    "여권 유효기간이 6개월 이상 남아야 하는 나라가 많아요. 성이 바뀌는 경우 " +
                                        "항공권 이름과 여권이 같아야 합니다. 식 직후 출발이면 피로를 감안하세요.",
                                dueOffsetDays = -45,
                                required = false,
                            ),
                            task(
                                key = "invitation",
                                title = "청첩장 보내기",
                                description =
                                    "한 달 전쯤이 적당해요. 너무 이르면 잊히고 너무 늦으면 일정을 비우기 어렵습니다. " +
                                        "모바일 청첩장만 보낼 사이와 종이로 전할 사이를 나눠 두면 수량이 줄어요.",
                                dueOffsetDays = -35,
                            ),
                            task(
                                key = "day-timeline",
                                title = "식 당일 순서 정리하기",
                                description =
                                    "메이크업 시작부터 식·사진·식사까지 시간표를 만들어 양가와 공유해요. " +
                                        "당일에는 누가 무엇을 들고 있어야 하는지(축의금·반지·서류)까지 정해 두면 덜 헤맵니다.",
                                dueOffsetDays = -7,
                                required = false,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "after",
                    title = "식이 끝난 뒤",
                    summary =
                        "여기가 가장 많이 빠뜨리는 단계예요. 식이 끝나면 준비가 끝난 것 같지만, " +
                            "법적으로 부부가 되는 일과 돈이 걸린 일은 이제부터입니다.",
                    tasks =
                        listOf(
                            task(
                                key = "marriage-report",
                                title = "혼인신고 하기",
                                description =
                                    "식을 올려도 신고하지 않으면 법적으로 부부가 아니에요. 성년 증인 두 명의 " +
                                        "서명이 필요하고, 신고일이 혼인 성립일이 됩니다. 대출·청약·보험의 " +
                                        "'혼인 기간' 요건이 모두 이 날짜 기준이라 미루면 그만큼 밀려요.",
                                dueOffsetDays = 14,
                                link = GuideLink("대한민국 법원 전자가족관계등록시스템", "https://efamily.scourt.go.kr"),
                            ),
                            task(
                                key = "insurance-dependent",
                                title = "건강보험·연금 정보 바꾸기",
                                description =
                                    "한쪽이 소득이 없으면 피부양자로 올릴 수 있어요. 회사에 혼인 사실을 알리면 " +
                                        "대개 함께 처리해 줍니다. 국민연금 분할 요건도 혼인 기간으로 셉니다.",
                                dueOffsetDays = 30,
                                link = SharedLinks.FOUR_INSURE,
                            ),
                            task(
                                key = "beneficiary",
                                title = "보험 수익자와 비상 연락처 바꾸기",
                                description =
                                    "수익자가 부모님으로 남아 있는 경우가 많아요. 사고가 났을 때 가장 늦게 " +
                                        "발견되는 종류의 미비입니다. 회사 비상 연락처도 함께 고치세요.",
                                dueOffsetDays = 45,
                                required = false,
                            ),
                            task(
                                key = "newlywed-benefit",
                                title = "신혼부부 지원 알아보기",
                                description =
                                    "주택 특별공급, 전세자금 대출, 지자체 지원금은 대개 혼인 기간 안에만 신청할 수 있어요. " +
                                        "기간이 지나면 되돌릴 수 없으니 신고 직후에 한 번 훑어보세요.",
                                dueOffsetDays = 60,
                                required = false,
                            ),
                            task(
                                key = "money-rule",
                                title = "둘의 돈 관리 방식 정하기",
                                description =
                                    "생활비를 어떻게 나누고, 각자 자유롭게 쓰는 몫은 얼마로 할지 정해요. " +
                                        "고정지출·변동지출 모듈에 함께 적어 두면 '얼마 쓰는지 모르겠다'가 사라집니다.",
                                dueOffsetDays = 90,
                                required = false,
                            ),
                        ),
                ),
            ),
    )
