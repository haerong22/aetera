"use client";

import { frontendModules } from "./registry";
import { useMyModules } from "./useMyModules";
import type { FrontendModule, ModuleCapabilities } from "./types";

function useEnabledModules(): FrontendModule[] {
  const { data: modules } = useMyModules();
  const enabledIds = new Set(modules?.filter((module) => module.enabled).map((module) => module.id));
  return frontendModules.filter((module) => enabledIds.has(module.id));
}

export function useAddEventDialog(): ModuleCapabilities["AddEventDialog"] | null {
  const provider = useEnabledModules().find((module) => module.capabilities?.AddEventDialog);
  return provider?.capabilities?.AddEventDialog ?? null;
}
