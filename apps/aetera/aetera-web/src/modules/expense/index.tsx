import { Wallet } from "lucide-react";
import type { FrontendModule } from "../types";
import { ExpensePage } from "./ExpensePage";
import { MonthlyFixedCost } from "./MonthlyFixedCost";

export const expenseModule: FrontendModule = {
  id: "expense",
  title: "고정지출",
  icon: Wallet,
  Page: ExpensePage,
  queryKeyPrefix: "expense",
  // 한 달 고정지출을 묻는 모듈이 있으면 여기서 답한다(퇴사 준비의 "버틸 개월 수").
  capabilities: { MonthlyFixedCost },
};
