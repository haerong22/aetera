import { Target } from "lucide-react";
import type { FrontendModule } from "../types";
import { GoalPage } from "./GoalPage";
import { GOAL_MODULE_ID } from "./id";

export const goalModule: FrontendModule = {
  id: GOAL_MODULE_ID,
  title: "목표",
  icon: Target,
  Page: GoalPage,
  queryKeyPrefix: GOAL_MODULE_ID,
};
