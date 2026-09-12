import { Banknote } from "lucide-react";
import type { FrontendModule } from "../types";
import { IncomePage } from "./IncomePage";
import { ContinuingIncome } from "./ContinuingIncome";
import { INCOME_MODULE_ID } from "./id";

export const incomeModule: FrontendModule = {
  id: INCOME_MODULE_ID,
  title: "소득",
  icon: Banknote,
  Page: IncomePage,
  queryKeyPrefix: INCOME_MODULE_ID,
  // 그만둬도 이어지는 한 달 소득을 묻는 모듈이 있으면 여기서 답한다(퇴사 준비의 "버틸 개월 수").
  capabilities: { ContinuingIncome },
};
