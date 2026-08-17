import type { ComponentType } from "react";
import type { LucideIcon } from "lucide-react";

export interface FrontendModule {
  id: string;
  title: string;
  icon: LucideIcon;
  Page: ComponentType;
  queryKeyPrefix: string;
  capabilities?: ModuleCapabilities;
}

export interface ModuleCapabilities {
  AddEventDialog?: ComponentType<CalendarDraftProps>;
}

export interface CalendarDraft {
  title: string;
  description?: string;
  date: string;
}

export interface CalendarDraftProps {
  open: boolean;
  onClose: () => void;
  draft: CalendarDraft;
}

export function modulePath(moduleId: string): string {
  return `/m/${moduleId}`;
}
