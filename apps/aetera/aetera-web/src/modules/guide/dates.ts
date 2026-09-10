import { daysUntil, fromLocalDateIso } from "@/lib/date";

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
