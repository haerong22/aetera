import type { LucideIcon } from "lucide-react";
import type { FrontendModule } from "../types";
import { GuidePage } from "./GuidePage";

export function guideModule({
  id,
  title,
  icon,
}: {
  id: string;
  title: string;
  icon: LucideIcon;
}): FrontendModule {
  function Page() {
    return <GuidePage moduleTitle={title} guideId={id} />;
  }
  Page.displayName = `Guide(${id})`;

  return { id, title, icon, Page, queryKeyPrefix: id };
}
