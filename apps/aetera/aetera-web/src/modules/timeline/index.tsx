import { History } from "lucide-react";
import type { FrontendModule } from "../types";
import { TimelinePage } from "./TimelinePage";

export const timelineModule: FrontendModule = {
  id: "timeline",
  title: "타임라인",
  icon: History,
  Page: TimelinePage,
  queryKeyPrefix: "timeline",
};
