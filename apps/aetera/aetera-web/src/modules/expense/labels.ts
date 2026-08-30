import type { ExpenseCategory, ExpenseCycle } from "./api";

export const CATEGORY_LABELS: Record<ExpenseCategory, string> = {
  HOUSING: "주거",
  UTILITY: "공과금",
  COMMUNICATION: "통신",
  INSURANCE: "보험",
  LOAN: "대출",
  SUBSCRIPTION: "구독",
  TRANSPORT: "교통",
  EDUCATION: "교육",
  ETC: "기타",
};

export const CYCLE_LABELS: Record<ExpenseCycle, string> = {
  MONTHLY: "매월",
  QUARTERLY: "3개월마다",
  HALF_YEARLY: "6개월마다",
  YEARLY: "매년",
};
