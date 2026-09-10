/** `"2026-09-30"` → `"9월 30일"`. 연도는 부르는 쪽이 이미 말한다. */
export function formatDay(iso: string): string {
  const [, month, day] = iso.split("-").map(Number);
  return `${month}월 ${day}일`;
}

/**
 * `daysUntil` 을 사람이 읽는 말로. **앞으로 올 것에만 쓴다.**
 *
 * 가이드의 `formatDDay` 와 짝이지만 말투가 다르다. 저쪽은 기준일 하나를 크게 보여주는
 * 자리라 `D-14` 가 어울리고, 여기는 여러 줄이 흐르는 목록이라 문장으로 읽히는 편이 낫다.
 */
export function untilLabel(days: number): string {
  if (days <= 0) return "오늘";
  if (days === 1) return "내일";
  return `${days}일 뒤`;
}
