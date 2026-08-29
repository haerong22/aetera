package io.aetera.usecase.guide

import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuidePhase
import io.aetera.model.guide.GuideTemplate
import java.time.MonthDay

/**
 * 연말정산 준비 가이드의 콘텐츠.
 *
 * 다른 가이드와 다른 점이 하나 있다: **기준일을 사용자가 고르지 않는다.** 퇴사일·이사일은
 * 사람마다 다르지만 연말정산의 기준은 언제나 12월 31일이다. 그래서 [GuideTemplate.anchorMonthDay]
 * 로 그 날을 못 박고, 시작 화면이 가장 가까운 12월 31일을 미리 채운다.
 *
 * 이 가이드의 핵심은 **연말까지 해야 줄어드는 것과 새해에 증명하는 것을 갈라 놓는 것**이다.
 * 1월에 서류를 아무리 잘 내도 12월 31일이 지나면 납입·결제로 줄일 수 있는 건 끝나 있다.
 */
internal val YEAR_END_TAX_GUIDE: GuideTemplate =
    GuideTemplate(
        id = GuideId("year-end-tax"),
        title = "연말정산 준비",
        summary = "12월 31일을 기준으로, 연말까지 움직여야 줄어드는 것부터 1월 서류 제출과 5월 추가 환급까지 짚어드려요.",
        anchorLabel = "정산 기준일",
        anchorMonthDay = MonthDay.of(12, 31),
        disclaimer =
            "공제 요건과 한도는 개인 상황과 세법 개정에 따라 달라져요. 금액이 큰 항목은 " +
                "국세청 상담이나 회사 담당자에게 한 번 더 확인하시는 걸 권해요.",
        phases =
            listOf(
                GuidePhase(
                    key = "before-year-end",
                    title = "연말까지 움직이기",
                    summary = "12월 31일이 지나면 끝나는 것들. 서류는 나중에 내도 되지만, 돈이 오가는 일은 올해 안에 해야 올해분이 돼요.",
                    tasks =
                        listOf(
                            task(
                                key = "card-mix",
                                title = "신용카드·체크카드 사용 비율 점검하기",
                                description =
                                    "총급여의 25%를 넘게 쓴 다음부터 공제가 시작돼요. 이미 넘겼다면 남은 기간은 " +
                                        "공제율이 두 배인 체크카드·현금영수증 쪽이 유리합니다.",
                                dueOffsetDays = -45,
                            ),
                            task(
                                key = "pension-account",
                                title = "연금저축·IRP 납입 한도 채우기",
                                description =
                                    "연금저축 600만원, IRP 를 합치면 900만원까지 세액공제를 받아요. " +
                                        "12월 31일까지 입금해야 올해분으로 잡히고, 총급여 5,500만원 이하면 16.5%, 넘으면 13.2% 를 돌려받습니다.",
                                dueOffsetDays = -30,
                            ),
                            task(
                                key = "medical-expense",
                                title = "간소화에 안 잡히는 의료비 영수증 챙기기",
                                description =
                                    "총급여의 3%를 넘는 의료비부터 공제돼요. 안경·콘택트렌즈(1인 50만원 한도), 산후조리원, " +
                                        "일부 의원 비용은 간소화 서비스에 안 뜨니 영수증을 따로 받아 두세요.",
                                dueOffsetDays = -30,
                                required = false,
                            ),
                            task(
                                key = "donation",
                                title = "기부금 영수증 챙기기",
                                description = "종교단체·지정기부금은 간소화에 안 올라오는 곳이 많아요. 발급 요청은 연말에 몰리니 미리 해 두면 편합니다.",
                                dueOffsetDays = -20,
                                required = false,
                            ),
                            task(
                                key = "housing-saving",
                                title = "주택청약저축 납입액 확인하기",
                                description =
                                    "무주택 세대주이고 총급여 7,000만원 이하면 연 300만원 납입까지 40% 를 소득공제받아요. " +
                                        "무주택 확인서를 은행에 내야 반영됩니다.",
                                dueOffsetDays = -20,
                                required = false,
                            ),
                            task(
                                key = "monthly-rent",
                                title = "월세액 세액공제 요건 확인하기",
                                description =
                                    "무주택 세대주, 총급여 8,000만원 이하, 전입신고 완료가 조건이에요. " +
                                        "임대차계약서와 계좌이체 내역이 있어야 하니 지금 모아 두세요.",
                                dueOffsetDays = -15,
                                required = false,
                            ),
                            task(
                                key = "dependents",
                                title = "부양가족 요건 확인하기",
                                description =
                                    "12월 31일 현재 상태로 판단해요. 연 소득 100만원(근로소득만 있으면 500만원) 이하여야 하고, " +
                                        "부모·형제자매는 나이 요건도 봅니다. 형제자매끼리 같은 부모를 중복으로 올리면 나중에 추징돼요.",
                                dueOffsetDays = -10,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "collect",
                    title = "서류 모으기",
                    summary = "1월 15일에 간소화 서비스가 열려요. 여기서 안 나오는 것만 따로 챙기면 됩니다.",
                    tasks =
                        listOf(
                            task(
                                key = "simplified-service",
                                title = "간소화 서비스에서 자료 내려받기",
                                description = "1월 15일에 열려요. 처음 열릴 때는 자료가 덜 올라와 있을 수 있으니 며칠 뒤 한 번 더 확인하는 편이 안전해요.",
                                dueOffsetDays = 15,
                                link = SharedLinks.HOMETAX,
                            ),
                            task(
                                key = "dependents-consent",
                                title = "부양가족 자료제공 동의받기",
                                description = "가족의 의료비·교육비는 본인 동의가 있어야 내 간소화 화면에 보여요. 만 19세 미만 자녀는 동의 없이 조회됩니다.",
                                dueOffsetDays = 16,
                                required = false,
                                link = SharedLinks.HOMETAX,
                            ),
                            task(
                                key = "missing-docs",
                                title = "간소화에 없는 서류 직접 발급받기",
                                description = "월세 계약서와 이체 내역, 교복·체육복, 중고생 학원비, 일부 기부금, 해외 교육비는 직접 챙겨야 해요.",
                                dueOffsetDays = 18,
                                required = false,
                            ),
                            task(
                                key = "submit-company",
                                title = "회사에 서류 제출하기",
                                description = "마감은 회사마다 달라요. 보통 1월 말에서 2월 초이니 사내 공지의 날짜를 확인하세요.",
                                dueOffsetDays = 30,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "settle",
                    title = "결과 확인하기",
                    summary = "2월 급여로 정산돼요. 놓친 게 있어도 되찾을 길이 남아 있습니다.",
                    tasks =
                        listOf(
                            task(
                                key = "check-result",
                                title = "2월 급여명세서에서 정산 결과 확인하기",
                                description = "환급이든 추가 납부든 2월 급여에 반영돼요. 낸 서류가 빠짐없이 반영됐는지 원천징수영수증으로 대조하세요.",
                                dueOffsetDays = 59,
                            ),
                            task(
                                key = "additional-may",
                                title = "놓친 공제는 5월 종합소득세로 되찾기",
                                description =
                                    "제출을 놓쳤거나 빠뜨린 공제가 있으면 5월 종합소득세 신고 기간에 넣을 수 있어요. " +
                                        "그때도 지나갔다면 경정청구로 5년까지 돌려받습니다.",
                                dueOffsetDays = 151,
                                required = false,
                                link = SharedLinks.HOMETAX,
                            ),
                        ),
                ),
            ),
    )
