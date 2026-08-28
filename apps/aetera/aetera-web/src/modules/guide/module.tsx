import type { LucideIcon } from "lucide-react";
import type { FrontendModule } from "../types";
import type { TaskToolMap } from "./types";
import { GuidePage } from "./GuidePage";

export function guideModule({
  id,
  title,
  icon,
  taskTools,
}: {
  id: string;
  title: string;
  icon: LucideIcon;
  taskTools?: TaskToolMap;
}): FrontendModule {
  function Page() {
    return <GuidePage moduleTitle={title} guideId={id} taskTools={taskTools} />;
  }
  Page.displayName = `Guide(${id})`;

  return { id, title, icon, Page, queryKeyPrefix: id };
}
