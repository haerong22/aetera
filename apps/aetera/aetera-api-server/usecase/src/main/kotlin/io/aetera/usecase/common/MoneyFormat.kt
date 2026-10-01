package io.aetera.usecase.common

import java.util.Locale

/**
 * 금액에 자릿점을 넣는다. `480000` → `480,000`.
 *
 * **로케일을 못 박는다.** 기본 로케일에 맡기면 컨테이너 설정에 따라 자릿점이 달라져
 * 같은 코드가 `480.000` 이나 `4,80,000` 을 낸다 — 타임라인과 알림 메일로 그대로 나가는 글이다.
 *
 * 단위(`원`)는 붙이지 않는다. 부르는 쪽이 "원"을 쓸지 "원 적음"을 쓸지 정한다.
 *
 * 모듈마다 두던 것을 여기로 모았다. 모듈은 서로 참조할 수 없어([ArchitectureTest]) 한쪽에
 * 두면 복제되는데, 두 벌이 되자 "로케일을 못 박는 이유" 주석까지 함께 복제됐다.
 */
fun money(amount: Long): String = String.format(Locale.KOREA, "%,d", amount)
