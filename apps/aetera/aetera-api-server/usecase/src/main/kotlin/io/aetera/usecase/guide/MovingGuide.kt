package io.aetera.usecase.guide

import io.aetera.model.guide.GuideId
import io.aetera.model.guide.GuideLink
import io.aetera.model.guide.GuidePhase
import io.aetera.model.guide.GuideTemplate

private object MovingLinks {
    val GOV24 = GuideLink("정부24", "https://www.gov.kr")
    val IROS = GuideLink("인터넷등기소", "https://www.iros.go.kr")
    val HUG = GuideLink("주택도시보증공사", "https://www.khug.or.kr")
    val EPOST = GuideLink("우체국", "https://www.epost.go.kr")
    val ECAR = GuideLink("자동차민원 대국민포털", "https://www.ecar.go.kr")
    val ANIMAL = GuideLink("동물보호관리시스템", "https://www.animal.go.kr")
}

internal val MOVING_GUIDE: GuideTemplate =
    GuideTemplate(
        id = GuideId("moving"),
        title = "이사 준비",
        summary = "이사 날짜를 정하면 무엇을 어떤 순서로 준비해야 하는지 알려드려요.",
        anchorLabel = "이사 예정일",
        disclaimer =
            "계약 형태와 지역에 따라 절차가 달라질 수 있어요. 보증금·기한이 걸린 항목은 " +
                "집주인이나 각 기관에 한 번 더 확인하시는 걸 권해요.",
        phases =
            listOf(
                GuidePhase(
                    key = "contract",
                    title = "계약과 확인",
                    summary = "돈이 가장 크게 걸린 단계예요. 여기서 놓치면 나중에 되돌리기 어려워요.",
                    tasks =
                        listOf(
                            task(
                                key = "notice-landlord",
                                title = "지금 집 계약 종료를 집주인에게 알리기",
                                description =
                                    "만료 전 정해진 기간 안에 알리지 않으면 계약이 같은 조건으로 자동 연장될 수 있어요. " +
                                        "문자나 메일처럼 기록이 남는 방법으로 남겨 두세요.",
                                dueOffsetDays = -60,
                            ),
                            task(
                                key = "budget-check",
                                title = "이사에 드는 돈 계산하기",
                                description =
                                    "보증금 차액, 이사비, 중개수수료, 입주청소, 수리비까지 더해 보세요. " +
                                        "보증금을 돌려받는 시점과 새 집에 내야 하는 시점이 어긋나면 잠깐 목돈이 필요해요.",
                                dueOffsetDays = -55,
                            ),
                            task(
                                key = "contract-check",
                                title = "새 집 등기부등본 확인하고 계약하기",
                                description =
                                    "근저당이 얼마나 잡혀 있는지, 집주인과 계약자가 같은 사람인지 확인해요. " +
                                        "보증금보다 앞선 채권이 많으면 돌려받지 못할 수 있어요.",
                                dueOffsetDays = -50,
                                link = MovingLinks.IROS,
                            ),
                            task(
                                key = "deposit-insurance",
                                title = "전세보증금 반환보증 알아보기",
                                description = "전세라면 보증금을 못 돌려받을 위험을 보험으로 덮을 수 있어요. 가입 조건과 시점을 미리 확인해 두세요.",
                                dueOffsetDays = -45,
                                required = false,
                                link = MovingLinks.HUG,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "booking",
                    title = "업체와 일정 잡기",
                    summary = "성수기에는 원하는 날짜가 금방 차요. 날짜가 정해졌으면 바로 예약하세요.",
                    tasks =
                        listOf(
                            task(
                                key = "mover-quotes",
                                title = "이삿짐센터 견적 비교하기",
                                description =
                                    "방문 견적을 두세 곳 받아 보세요. 짐 양을 보지 않고 전화로만 부르는 금액은 " +
                                        "당일에 바뀌는 경우가 많아요.",
                                dueOffsetDays = -40,
                            ),
                            task(
                                key = "mover-contract",
                                title = "이삿짐센터 계약하기",
                                description = "포장 범위, 사다리차 포함 여부, 파손 시 보상 조건을 계약서에 적어 두세요.",
                                dueOffsetDays = -35,
                            ),
                            task(
                                key = "elevator-booking",
                                title = "엘리베이터·사다리차 예약하기",
                                description =
                                    "아파트라면 관리사무소에 이사 일정을 알리고 엘리베이터를 잡아 두세요. " +
                                        "같은 날 다른 집이 먼저 예약했으면 시간을 통째로 바꿔야 해요.",
                                dueOffsetDays = -30,
                            ),
                            task(
                                key = "cleaning-booking",
                                title = "입주청소 예약하기",
                                description = "짐이 들어가기 전에 하는 게 훨씬 깨끗하고 싸요. 이사 전날이나 당일 오전으로 잡으면 좋아요.",
                                dueOffsetDays = -25,
                                required = false,
                            ),
                            task(
                                key = "aircon-move",
                                title = "에어컨 이전 설치 예약하기",
                                description = "이삿짐센터가 해 주지 않는 경우가 많아요. 벽걸이·스탠드에 따라 비용과 시간이 달라집니다.",
                                dueOffsetDays = -21,
                                required = false,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "prepare",
                    title = "짐과 서비스 정리",
                    summary = "옮길 것과 버릴 것을 나누고, 끊고 옮겨야 할 서비스를 처리해요.",
                    tasks =
                        listOf(
                            task(
                                key = "declutter",
                                title = "버릴 것 먼저 정리하기",
                                description = "짐 양이 곧 이사비예요. 안 쓰는 물건을 미리 줄이면 견적도 당일 작업 시간도 함께 줄어요.",
                                dueOffsetDays = -20,
                                required = false,
                            ),
                            task(
                                key = "bulk-waste",
                                title = "대형폐기물 스티커 신청하기",
                                description = "가구·가전은 그냥 버릴 수 없어요. 주민센터나 구청 앱에서 신청하고 스티커를 붙여 정해진 날에 내놓아요.",
                                dueOffsetDays = -14,
                                required = false,
                            ),
                            task(
                                key = "internet-move",
                                title = "인터넷·TV 이전 신청하기",
                                description =
                                    "설치 기사 일정이 밀리면 이사 후 며칠을 인터넷 없이 보내요. " +
                                        "약정이 남았는지, 이전 설치비가 있는지도 같이 확인하세요.",
                                dueOffsetDays = -14,
                            ),
                            task(
                                key = "gas-disconnect",
                                title = "도시가스 전출 예약하기",
                                description = "이사 당일 방문 철거가 필요해요. 예약이 밀리면 가스를 끊지 못한 채 집을 비우게 됩니다.",
                                dueOffsetDays = -7,
                            ),
                            task(
                                key = "school-transfer",
                                title = "자녀 전학 절차 확인하기",
                                description = "학교마다 필요한 서류와 시점이 달라요. 전입신고 후에 진행되는 절차가 있으니 순서를 미리 물어보세요.",
                                dueOffsetDays = -7,
                                required = false,
                            ),
                            task(
                                key = "packing",
                                title = "미리 쌀 짐 싸 두기",
                                description = "책, 계절 옷처럼 당장 안 쓰는 것부터 싸요. 상자마다 어느 방으로 갈지 적어 두면 푸는 시간이 확 줄어요.",
                                dueOffsetDays = -3,
                                required = false,
                            ),
                            task(
                                key = "valuables",
                                title = "귀중품과 중요 서류 따로 챙기기",
                                description =
                                    "현금, 통장, 도장, 계약서, 노트북은 이삿짐에 섞지 말고 직접 들고 다니세요. " +
                                        "분실해도 이삿짐 보상으로는 잘 해결되지 않아요.",
                                dueOffsetDays = -1,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "movingday",
                    title = "이사 당일",
                    summary = "집을 비우기 전에 정산을 끝내야 해요. 나오고 나면 연락이 어려워집니다.",
                    tasks =
                        listOf(
                            task(
                                key = "meter-reading",
                                title = "전기·수도·가스 검침하고 정산하기",
                                description = "떠나는 날까지 쓴 만큼만 내면 돼요. 검침 숫자를 사진으로 남겨 두면 나중에 다투지 않아요.",
                                dueOffsetDays = 0,
                            ),
                            task(
                                key = "maintenance-fee",
                                title = "관리비 정산하기",
                                description = "관리사무소에 이사 당일 기준으로 정산을 요청해요. 선수관리비를 냈다면 돌려받을 것이 있는지도 확인하세요.",
                                dueOffsetDays = 0,
                            ),
                            task(
                                key = "repair-reserve",
                                title = "장기수선충당금 돌려받기",
                                description =
                                    "아파트에 세들어 살았다면 관리비에 포함돼 나간 이 돈은 원래 집주인 몫이에요. " +
                                        "관리사무소에서 납부 내역을 떼어 집주인에게 청구하세요. 모르면 그냥 넘어가는 돈입니다.",
                                dueOffsetDays = 0,
                            ),
                            task(
                                key = "deposit-return",
                                title = "보증금 돌려받은 것 확인하기",
                                description = "집을 비우고 열쇠를 넘기기 전에 입금을 확인하세요. 순서가 바뀌면 협상할 카드가 사라져요.",
                                dueOffsetDays = 0,
                            ),
                            task(
                                key = "key-handover",
                                title = "열쇠·카드키 넘기고 집 상태 확인받기",
                                description = "집주인과 함께 집을 둘러보고 수리비 이야기를 끝내요. 비운 뒤 상태를 사진으로 남겨 두면 좋아요.",
                                dueOffsetDays = 0,
                            ),
                        ),
                ),
                GuidePhase(
                    key = "settle",
                    title = "이사 후 신고와 변경",
                    summary = "여기가 진짜예요. 늦으면 보증금 보호를 못 받거나 과태료가 붙어요.",
                    tasks =
                        listOf(
                            task(
                                key = "move-in-report",
                                title = "전입신고하기",
                                description =
                                    "전입한 날부터 정해진 기한 안에 해야 하고, 늦으면 과태료가 있어요. " +
                                        "보증금을 지키는 대항력도 전입신고가 있어야 생기니 가장 먼저 하세요.",
                                dueOffsetDays = 1,
                                link = MovingLinks.GOV24,
                            ),
                            task(
                                key = "fixed-date",
                                title = "확정일자 받기",
                                description =
                                    "전입신고와 함께 받아 두면 보증금을 돌려받을 순위가 생겨요. " +
                                        "이게 없으면 집이 경매로 넘어갔을 때 뒤로 밀립니다.",
                                dueOffsetDays = 1,
                                link = MovingLinks.GOV24,
                            ),
                            task(
                                key = "gas-connect",
                                title = "도시가스 전입 신청하기",
                                description = "새 집에서 가스를 쓰려면 연결 방문이 필요해요. 이사 당일이나 다음 날로 미리 잡아 두면 찬물로 씻지 않아요.",
                                dueOffsetDays = 1,
                            ),
                            task(
                                key = "address-change",
                                title = "은행·카드·보험 주소 바꾸기",
                                description = "청구서와 갱신 안내가 옛 주소로 가면 놓치기 쉬워요. 주로 쓰는 곳부터 한 번에 처리하세요.",
                                dueOffsetDays = 7,
                            ),
                            task(
                                key = "mail-forward",
                                title = "우편물 주소 이전 서비스 신청하기",
                                description = "옛 주소로 오는 우편물을 새 집으로 일정 기간 보내 줘요. 빠뜨린 주소 변경을 찾아내는 데도 도움이 돼요.",
                                dueOffsetDays = 7,
                                required = false,
                                link = MovingLinks.EPOST,
                            ),
                            task(
                                key = "car-address",
                                title = "자동차 주소 변경하기",
                                description = "정해진 기한 안에 하지 않으면 과태료가 붙어요. 자동차세 고지서도 새 주소로 가야 합니다.",
                                dueOffsetDays = 14,
                                link = MovingLinks.ECAR,
                            ),
                            task(
                                key = "pet-address",
                                title = "반려동물 등록 정보 바꾸기",
                                description = "등록된 주소가 옛집이면 잃어버렸을 때 연락이 닿지 않아요.",
                                dueOffsetDays = 30,
                                required = false,
                                link = MovingLinks.ANIMAL,
                            ),
                        ),
                ),
            ),
    )
