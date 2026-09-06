package io.aetera.usecase.guide

import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuidePhase
import io.aetera.model.guide.GuideTemplate

/**
 * 이직 준비 가이드의 콘텐츠.
 *
 * 퇴사 준비와 짝이지만 **다루는 쪽이 다르다.** 저쪽은 나가는 일(통보·인수인계·정산·실업급여)을,
 * 여기는 들어가는 일(지원·처우·입사일·수습)을 본다. 겹치는 항목은 넣지 않았다 —
 * 두 모듈을 함께 켠 사람이 같은 할 일을 두 번 보면 어느 쪽을 체크해야 할지 알 수 없다.
 *
 * 기준일이 **입사 예정일**이라 시작할 때는 대개 모른다. 대충 잡고 나중에 바꾸면 되고,
 * 그때도 체크해 둔 항목은 남는다(엔진이 보장한다).
 */
internal val JOB_CHANGE_GUIDE: GuideTemplate =
    GuideTemplate(
        id = GuideId("job-change"),
        title = "이직 준비",
        summary = "입사 예정일을 정하면 지원 서류부터 처우 협의, 보험 공백, 수습까지 순서대로 짚어드려요.",
        anchorLabel = "입사 예정일",
        disclaimer =
            "회사와 직무에 따라 절차가 크게 달라져요. 처우와 입사일은 반드시 문서로 확인하시고, " +
                "구두로 오간 약속은 메일로 한 번 정리해 두시는 걸 권해요.",
        phases =
            listOf(
                GuidePhase(
                    key = "apply",
                    title = "지원하기",
                    summary = "재직 중에 해 두는 게 훨씬 수월해요. 기억이 생생하고, 급하게 쫓기지 않아요.",
                    tasks =
                        listOf(
                            task(
                                key = "resume",
                                title = "이력서와 경력기술서 쓰기",
                                description =
                                    "맡은 업무를 나열하는 대신, 무엇이 문제였고 어떻게 했고 결과가 어땠는지를 적어요. " +
                                        "읽는 사람에게 남는 건 한 일이 아니라 바꾼 것입니다.",
                                dueOffsetDays = -90,
                            ),
                            task(
                                key = "portfolio",
                                title = "포트폴리오 정리하기",
                                description =
                                    "회사 자산이 아닌 것만 골라요. 기밀·고객 정보가 섞이면 지원한 회사에도 부담이 됩니다. " +
                                        "직무에 따라 필요 없을 수도 있어요.",
                                dueOffsetDays = -85,
                                required = false,
                            ),
                            task(
                                key = "company-research",
                                title = "지원할 회사 조사하기",
                                description =
                                    "재무 상태, 최근 뉴스, 조직 개편 이력을 봐요. 연봉만 보고 옮기면 " +
                                        "1년 뒤 같은 고민을 다시 하게 됩니다.",
                                dueOffsetDays = -70,
                                required = false,
                            ),
                            task(
                                key = "reference-ask",
                                title = "레퍼런스 부탁해 두기",
                                description =
                                    "요청이 오면 급합니다. 미리 양해를 구해 두면 상대도 준비할 시간이 생기고, " +
                                        "누가 어떤 면을 말해 줄지 고를 수 있어요.",
                                dueOffsetDays = -45,
                                required = false,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "offer",
                    title = "조건 맞추기",
                    summary = "말로 오간 것은 남지 않아요. 이 단계에서 문서로 만들어 두면 나중에 다툴 일이 없습니다.",
                    tasks =
                        listOf(
                            task(
                                key = "offer-review",
                                title = "처우 조건 문서로 받기",
                                description =
                                    "연봉, 직급, 담당 업무, 근무지, 입사일이 적힌 처우 협의서나 메일을 받아 두세요. " +
                                        "구두 약속은 담당자가 바뀌면 사라집니다.",
                                dueOffsetDays = -30,
                            ),
                            task(
                                key = "total-comp",
                                title = "연봉 외 조건 따져 보기",
                                description =
                                    "사이닝 보너스의 반환 조건, 성과급 지급 시기와 기준, 스톡옵션 행사 조건, " +
                                        "연차 승계 여부, 재택 규정까지 합쳐야 실제로 손에 남는 것이 보여요.",
                                dueOffsetDays = -25,
                            ),
                            task(
                                key = "start-date",
                                title = "입사일 조율하기",
                                description =
                                    "지금 회사의 통보 기간과 인수인계에 걸리는 시간을 먼저 계산하고 제안하세요. " +
                                        "무리하게 당기면 남는 쪽과 가는 쪽 모두에 부담이 됩니다.",
                                dueOffsetDays = -20,
                            ),
                            task(
                                key = "probation",
                                title = "수습 조건 확인하기",
                                description =
                                    "수습 기간의 급여 비율, 평가 기준, 수습 종료 시점을 미리 물어보세요. " +
                                        "수습도 근로계약이라 조건이 문서에 있어야 합니다.",
                                dueOffsetDays = -15,
                                required = false,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "join",
                    title = "옮기고 자리 잡기",
                    summary = "퇴사일과 입사일 사이가 벌어지면 그 기간은 온전히 내 몫이에요.",
                    tasks =
                        listOf(
                            task(
                                key = "insurance-gap",
                                title = "퇴사일과 입사일 사이 보험 공백 확인하기",
                                description =
                                    "퇴사일 다음 날 바로 입사하면 공백이 없어요. 며칠이라도 비면 그 사이에는 " +
                                        "지역가입자가 되니, 공백이 길어질 것 같으면 임의계속가입이 유리한지 따져 보세요.",
                                dueOffsetDays = -7,
                                link = SharedLinks.FOUR_INSURE,
                            ),
                            task(
                                key = "onboarding-docs",
                                title = "입사 서류 챙기기",
                                description =
                                    "주민등록등본, 급여 통장, 자격증 사본, 최종 학력 증명서를 흔히 요구해요. " +
                                        "전 직장 원천징수영수증도 새 회사 연말정산에 필요합니다.",
                                dueOffsetDays = -3,
                            ),
                            task(
                                key = "first-week",
                                title = "첫 주에 물어볼 것 적어 두기",
                                description =
                                    "평가 주기, 승진 기준, 팀의 우선순위, 내게 기대하는 첫 성과. " +
                                        "석 달 지나면 묻기 어색해지는 것들이라 지금 적어 두세요.",
                                dueOffsetDays = 3,
                                required = false,
                            ),
                            task(
                                key = "probation-check",
                                title = "수습 끝나기 전에 피드백 받기",
                                description =
                                    "통보를 기다리지 말고 먼저 물어보세요. 남은 기간에 고칠 수 있는 것과 " +
                                        "못 고치는 것을 가르는 유일한 방법입니다.",
                                dueOffsetDays = 60,
                                required = false,
                            ),
                        ),
                ),
            ),
    )
