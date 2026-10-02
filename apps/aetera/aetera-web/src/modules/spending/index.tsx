import { Receipt } from "lucide-react";
import type { FrontendModule } from "../types";
import { SpendingPage } from "./SpendingPage";
import { MonthlyVariableCost } from "./MonthlyVariableCost";
import { SPENDING_MODULE_ID } from "./id";

export const spendingModule: FrontendModule = {
  id: SPENDING_MODULE_ID,
  title: "변동지출",
  icon: Receipt,
  Page: SpendingPage,
  queryKeyPrefix: SPENDING_MODULE_ID,
  // "한 달에 얼마 쓰나"를 묻는 모듈이 있으면 여기서 답한다 — 지난 달들의 평균으로.
  capabilities: { MonthlyVariableCost },
};
