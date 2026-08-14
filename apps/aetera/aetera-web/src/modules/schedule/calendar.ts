import { toLocalDateIso } from "@/lib/date";

/** 달력 계산 유틸 — 의존성 없이 Date 만 쓴다. */

export interface CalendarCell {
  date: Date;
  inCurrentMonth: boolean;
  isToday: boolean;
}

export function startOfDay(date: Date): Date {
  const copy = new Date(date);
  copy.setHours(0, 0, 0, 0);
  return copy;
}

export function endOfDay(date: Date): Date {
  const copy = new Date(date);
  copy.setHours(23, 59, 59, 999);
  return copy;
}

export function isSameDay(a: Date, b: Date): boolean {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}

/** 해당 월을 감싸는 6주(42칸) 그리드. 일요일 시작. */
export function monthGrid(year: number, month: number): CalendarCell[] {
  const firstOfMonth = new Date(year, month, 1);
  const gridStart = new Date(firstOfMonth);
  gridStart.setDate(1 - firstOfMonth.getDay());

  const today = new Date();
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(gridStart);
    date.setDate(gridStart.getDate() + index);
    return {
      date,
      inCurrentMonth: date.getMonth() === month,
      isToday: isSameDay(date, today),
    };
  });
}

/** 그리드 전체를 덮는 조회 범위. */
export function monthRange(year: number, month: number): { from: Date; to: Date } {
  const cells = monthGrid(year, month);
  return {
    from: startOfDay(cells[0].date),
    to: endOfDay(cells[cells.length - 1].date),
  };
}

/** `datetime-local` input 값 (로컬 타임존 기준). */
export function toDateTimeLocal(date: Date): string {
  const pad = (value: number) => String(value).padStart(2, "0");
  return `${toLocalDateIso(date)}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" });
}
