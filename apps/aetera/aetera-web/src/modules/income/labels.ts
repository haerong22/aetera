import type { IncomeCategory, IncomeCycle } from "./api";

export const CATEGORY_LABELS: Record<IncomeCategory, string> = {
  SALARY: "월급",
  SIDE: "부업",
  RENTAL: "임대",
  FINANCIAL: "금융",
  PENSION: "연금",
  BENEFIT: "지원금",
  ETC: "기타",
};

export const CYCLE_LABELS: Record<IncomeCycle, string> = {
  MONTHLY: "매월",
  QUARTERLY: "3개월마다",
  HALF_YEARLY: "6개월마다",
  YEARLY: "매년",
};
