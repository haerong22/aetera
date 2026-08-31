import type { ComponentType, ReactNode } from "react";
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
  MonthlyFixedCost?: ComponentType<MonthlyFixedCostProps>;
}

/**
 * 값이 아니라 컴포넌트로 건넨다.
 *
 * 숫자를 주려면 훅이 필요한데, 능력은 켜진 모듈에서 **찾아낸** 것이라 있을 수도 없을 수도 있다.
 * 훅을 조건부로 부를 수는 없으므로, 데이터를 읽는 훅은 제공하는 쪽 컴포넌트 안에 두고
 * 결과만 자식 함수로 넘긴다.
 */
export interface MonthlyFixedCostProps {
  /** 아직 못 읽었으면 `null`. 등록한 항목이 없으면 `0`. */
  children: (monthlyTotal: number | null) => ReactNode;
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
