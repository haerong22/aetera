import { fromLocalDateIso, localToday, toLocalDateIso } from "@/lib/date";

/** `2026-09-01` → `2026년 9월`. */
export function monthLabel(iso: string): string {
  const date = fromLocalDateIso(iso);
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월`;
}

/** 이번 달의 1일. 서버가 보내는 달 값과 같은 모양이다. */
export function currentMonthIso(): string {
  const date = localToday();
  date.setDate(1);
  return toLocalDateIso(date);
}

/**
 * 직전 달의 1일.
 *
 * "이번 달 적기"보다 이쪽이 기본인 경우가 많다 — 달이 끝나야 쓴 돈이 확정되고,
 * 카드 명세서도 그때 나온다.
 */
export function previousMonthIso(): string {
  const date = localToday();
  date.setDate(1);
  date.setMonth(date.getMonth() - 1);
  return toLocalDateIso(date);
}
