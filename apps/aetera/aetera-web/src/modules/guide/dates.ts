import { fromLocalDateIso, localToday } from "@/lib/date";

const MILLIS_PER_DAY = 86_400_000;

function daysUntil(iso: string): number {
  return Math.round((fromLocalDateIso(iso).getTime() - localToday().getTime()) / MILLIS_PER_DAY);
}

export function formatKoreanDate(iso: string): string {
  return fromLocalDateIso(iso).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

export function formatDDay(iso: string): string {
  const days = daysUntil(iso);
  if (days === 0) return "D-DAY";
  return days > 0 ? `D-${days}` : `D+${-days}`;
}
