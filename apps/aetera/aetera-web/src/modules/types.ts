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
  /** 한 달에 나가는 고정지출. */
  MonthlyFixedCost?: ComponentType<AmountProviderProps>;
  /** 당장 쓸 수 있는 현금. */
  CashOnHand?: ComponentType<AmountProviderProps>;
}

/**
 * 능력이 건네는 금액 한 덩어리.
 *
 * 금액만 주면 부르는 쪽이 그게 **언제 값인지** 알 수 없다. 자산처럼 달마다 찍는 기록은
 * 석 달 전 잔액이 "지금 가진 돈" 자리에 조용히 앉을 수 있어, 어디서 온 값인지 함께 말한다.
 */
export interface ProvidedAmount {
  amount: number;
  /** 금액 아래에 함께 보일 한 줄. "2026년 9월 기준" 처럼 언제 값인지 밝힌다. */
  note?: string;
}

/**
 * 값이 아니라 컴포넌트로 건넨다.
 *
 * 숫자를 주려면 훅이 필요한데, 능력은 켜진 모듈에서 **찾아낸** 것이라 있을 수도 없을 수도 있다.
 * 훅을 조건부로 부를 수는 없으므로, 데이터를 읽는 훅은 제공하는 쪽 컴포넌트 안에 두고
 * 결과만 자식 함수로 넘긴다.
 */
export interface AmountProviderProps {
  /** 아직 못 읽었으면 `null`. */
  children: (provided: ProvidedAmount | null) => ReactNode;
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
