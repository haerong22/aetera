import type { AssetCategory } from "./api";

export const CATEGORY_LABELS: Record<AssetCategory, string> = {
  CASH: "현금",
  INVESTMENT: "투자",
  REAL_ESTATE: "부동산",
  PENSION: "연금",
  DEBT: "부채",
  ETC: "기타",
};

/** `"2026-09-01"` → `"2026년 9월"`. 달 단위 기록이라 일은 말하지 않는다. */
export function formatMonth(iso: string): string {
  const [year, month] = iso.split("-").map(Number);
  return `${year}년 ${month}월`;
}
