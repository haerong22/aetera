"use client";

import { frontendModules } from "./registry";
import { useMyModules } from "./useMyModules";
import type { FrontendModule, ModuleCapabilities } from "./types";

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
function useCapability<K extends keyof ModuleCapabilities>(name: K): ModuleCapabilities[K] | null {
  const provider = useEnabledModules().find((module) => module.capabilities?.[name]);
  return provider?.capabilities?.[name] ?? null;
}

export function useAddEventDialog(): ModuleCapabilities["AddEventDialog"] | null {
  return useCapability("AddEventDialog");
}

export function useMonthlyFixedCost(): ModuleCapabilities["MonthlyFixedCost"] | null {
  return useCapability("MonthlyFixedCost");
}
