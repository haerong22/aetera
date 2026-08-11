import { CalendarDays } from "lucide-react";
import type { FrontendModule } from "../types";
import { SchedulePage } from "./SchedulePage";
import { SCHEDULE_MODULE_ID } from "./id";

/** 백엔드 `ScheduleModule`(ModuleId "schedule") 의 프론트엔드 짝. */
export const scheduleModule: FrontendModule = {
  id: SCHEDULE_MODULE_ID,
  title: "일정",
  icon: CalendarDays,
  Page: SchedulePage,
  queryKeyPrefix: SCHEDULE_MODULE_ID,
};
