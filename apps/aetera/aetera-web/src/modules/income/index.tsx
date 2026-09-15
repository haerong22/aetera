import { Banknote } from "lucide-react";
import type { FrontendModule } from "../types";
import { IncomePage } from "./IncomePage";
import { ContinuingIncome } from "./ContinuingIncome";
import { MonthlyIncome } from "./MonthlyIncome";
import { INCOME_MODULE_ID } from "./id";

export const incomeModule: FrontendModule = {
  id: INCOME_MODULE_ID,
  title: "소득",
  icon: Banknote,
  Page: IncomePage,
  queryKeyPrefix: INCOME_MODULE_ID,
  // 소득을 묻는 모듈이 있으면 여기서 답한다. 둘로 나눈 이유는 묻는 것이 달라서다 —
  // 퇴사 준비는 "그만두면 얼마 남나", 홈 화면은 "지금 얼마 들어오나".
  capabilities: { ContinuingIncome, MonthlyIncome },
};
