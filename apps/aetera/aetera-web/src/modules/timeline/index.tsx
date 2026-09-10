import { History } from "lucide-react";
import type { FrontendModule } from "../types";
import { TimelinePage } from "./TimelinePage";
import { TIMELINE_MODULE_ID } from "./id";

export const timelineModule: FrontendModule = {
  id: TIMELINE_MODULE_ID,
  title: "타임라인",
  icon: History,
  Page: TimelinePage,
  queryKeyPrefix: TIMELINE_MODULE_ID,
};
