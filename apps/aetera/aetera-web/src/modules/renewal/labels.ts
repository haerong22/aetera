import { fromLocalDateIso, localToday } from "@/lib/date";
import type { Renewal, RenewalCategory, RenewalCycle } from "./api";

export const CATEGORY_LABELS: Record<RenewalCategory, string> = {
  INSURANCE: "보험",
  CONTRACT: "계약",
  CERTIFICATE: "증명서",
  SUBSCRIPTION: "구독",
  HEALTH: "건강",
  ETC: "기타",
};

export const CYCLE_LABELS: Record<RenewalCycle, string> = {
  NONE: "반복 없음",
  MONTHLY: "1개월",
  QUARTERLY: "3개월",
  HALF_YEARLY: "6개월",
  YEARLY: "1년",
  TWO_YEARS: "2년",
  FIVE_YEARS: "5년",
  TEN_YEARS: "10년",
};

const MILLIS_PER_DAY = 86_400_000;

function daysUntilExpiry(renewal: Renewal): number {
  return Math.round(
    (fromLocalDateIso(renewal.expiresAt).getTime() - localToday().getTime()) / MILLIS_PER_DAY,
  );
}

type RenewalStatus = "expired" | "due" | "fine";

/**
 * 만기 상태는 브라우저가 정한다 — 서버는 사용자의 로컬 날짜를 모른다.
 * "언제부터 급한가"는 항목마다 다르므로 noticeDays 를 쓴다(여권은 6개월 전, 보험은 한 달 전).
 */
export function renewalStatus(renewal: Renewal): RenewalStatus {
  const days = daysUntilExpiry(renewal);
  if (days < 0) return "expired";
  return days <= renewal.noticeDays ? "due" : "fine";
}

export function formatExpiry(renewal: Renewal): string {
  const days = daysUntilExpiry(renewal);
  if (days === 0) return "오늘 만기";
  if (days < 0) return `${-days}일 지남`;
  return `${days}일 남음`;
}
