"use client";

import type { ComponentType, ReactNode } from "react";
import { frontendModules } from "./registry";
import { useMyModules } from "./useMyModules";
import type { AmountProviderProps, FrontendModule, ModuleCapabilities, ProvidedAmount } from "./types";

/**
 * 찾았거나(컴포넌트) 못 찾았거나(`null`).
 *
 * [ModuleCapabilities] 의 필드가 선택이라 그대로 쓰면 `undefined` 까지 섞여 세 갈래가 된다 —
 * 부르는 쪽은 "있다/없다" 두 갈래만 다루면 된다.
 */
type Capability<K extends keyof ModuleCapabilities> = NonNullable<ModuleCapabilities[K]> | null;

function useEnabledModules(): FrontendModule[] {
  const { data: modules } = useMyModules();
  const enabledIds = new Set(modules?.filter((module) => module.enabled).map((module) => module.id));
  return frontendModules.filter((module) => enabledIds.has(module.id));
}

/**
 * 그 능력을 내놓는 **켜진** 모듈을 찾는다. 없으면 `null` — 부르는 쪽은 그 자리를 비운다.
 *
 * 꺼진 모듈의 능력은 보이지 않는다. 사용자가 끈 모듈이 다른 화면에서 계속 일하고 있으면
 * "중지"가 무슨 뜻인지 알 수 없게 된다.
 */
function useCapability<K extends keyof ModuleCapabilities>(name: K): Capability<K> {
  const provider = useEnabledModules().find((module) => module.capabilities?.[name]);
  return provider?.capabilities?.[name] ?? null;
}

export function useAddEventDialog(): Capability<"AddEventDialog"> {
  return useCapability("AddEventDialog");
}

export function useMonthlyFixedCost(): Capability<"MonthlyFixedCost"> {
  return useCapability("MonthlyFixedCost");
}

export function useCashOnHand(): Capability<"CashOnHand"> {
  return useCapability("CashOnHand");
}

/**
 * 능력이 있으면 그 값을, 없으면 `null` 을 준 채로 자식을 그린다.
 *
 * 금액을 둘 이상 받는 화면이 "있음/없음"을 손으로 엮으면 조합이 금세 네 가지가 된다.
 * 여기서 한 번 접어 두면 부르는 쪽은 언제나 같은 모양으로 쓴다.
 */
export function WithAmount({
  provider: Provider,
  children,
}: {
  provider: ComponentType<AmountProviderProps> | null;
  children: (provided: ProvidedAmount | null) => ReactNode;
}) {
  return Provider ? <Provider>{children}</Provider> : <>{children(null)}</>;
}
