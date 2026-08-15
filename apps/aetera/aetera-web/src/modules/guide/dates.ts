import { toLocalDateIso } from "@/lib/date";

/**
 * `YYYY-MM-DD` 문자열 다루기.
 *
 * `new Date("2026-09-30")` 은 UTC 자정으로 해석되므로, 로컬 자정인 "오늘"과 빼면
 * 타임존만큼 어긋나 D-day 가 하루 밀린다. 그래서 문자열을 직접 쪼개 **로컬 자정**으로 만든다.
 *
 * 서버가 날짜(시각 아님)만 내려주는 것도 같은 이유다 — 사용자가 달력에서 고른 그 날이
 * 그대로 값이면 변환이 끼어들 여지가 없다.
 */
function parseLocalDate(iso: string): Date {
  const [year, month, day] = iso.split("-").map(Number);
  return new Date(year, month - 1, day);
}

/** 오늘의 로컬 자정. 브라우저가 사용자의 진짜 오늘을 알고 있다. */
function localToday(): Date {
  const now = new Date();
  return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}

const MILLIS_PER_DAY = 86_400_000;

/** `to - from` 일수. 양쪽 다 로컬 자정이라 반올림으로 서머타임 오차를 흡수한다. */
function daysBetween(from: Date, to: Date): number {
  return Math.round((to.getTime() - from.getTime()) / MILLIS_PER_DAY);
}

export function formatKoreanDate(iso: string): string {
  return parseLocalDate(iso).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

export function formatShortDate(iso: string): string {
  return parseLocalDate(iso).toLocaleDateString("ko-KR", { month: "long", day: "numeric" });
}

/** 오늘로부터 `days` 일 뒤의 날짜. 기준일 입력(`<input type="date">`)의 기본값으로 쓴다. */
export function isoFromToday(days: number): string {
  const date = localToday();
  date.setDate(date.getDate() + days);
  return toLocalDateIso(date);
}

/** D-day 표기. 오늘이면 D-DAY, 지났으면 D+n. */
export function formatDDay(iso: string): string {
  const days = daysBetween(localToday(), parseLocalDate(iso));
  if (days === 0) return "D-DAY";
  return days > 0 ? `D-${days}` : `D+${-days}`;
}

type DueTone = "overdue" | "soon" | "normal";

/**
 * 마감의 긴급도. 이미 끝낸 항목은 언제 마감이었든 재촉하지 않는다 —
 * 다 한 일에 빨간 표시가 남아 있으면 진짜 밀린 것을 못 찾는다.
 */
export function dueTone(dueDate: string | null | undefined, done: boolean): DueTone {
  if (!dueDate || done) return "normal";
  const days = daysBetween(localToday(), parseLocalDate(dueDate));
  if (days < 0) return "overdue";
  return days <= 3 ? "soon" : "normal";
}
