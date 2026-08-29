export function toLocalDateIso(date: Date): string {
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  return `${date.getFullYear()}-${month}-${day}`;
}

export function fromLocalDateIso(iso: string): Date {
  const [year, month, day] = iso.split("-").map(Number);
  return new Date(year, month - 1, day);
}

export function localToday(): Date {
  const now = new Date();
  return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}

export function isoFromToday(days: number): string {
  const date = localToday();
  date.setDate(date.getDate() + days);
  return toLocalDateIso(date);
}

/**
 * `"12-31"` 처럼 매년 돌아오는 날짜 중 **오늘과 가장 가까운** 해의 날.
 *
 * "다음 12월 31일" 로 잡으면 안 된다 — 1월에 연말정산을 하는 사람이 1년 뒤 날짜를 받는다.
 * 방금 지나간 12월 31일이 그 사람이 정산하는 해다.
 */
export function nearestIsoOccurrence(monthDay: string): string {
  const [month, day] = monthDay.split("-").map(Number);
  const today = localToday();
  const distance = (date: Date) => Math.abs(date.getTime() - today.getTime());

  const nearest = [-1, 0, 1]
    .map((yearOffset) => new Date(today.getFullYear() + yearOffset, month - 1, day))
    .reduce((best, candidate) => (distance(candidate) < distance(best) ? candidate : best));

  return toLocalDateIso(nearest);
}
