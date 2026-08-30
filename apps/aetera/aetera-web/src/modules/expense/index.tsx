import { Wallet } from "lucide-react";
import type { FrontendModule } from "../types";
import { ExpensePage } from "./ExpensePage";

export const expenseModule: FrontendModule = {
  id: "expense",
  title: "고정지출",
  icon: Wallet,
  Page: ExpensePage,
  queryKeyPrefix: "expense",
};
