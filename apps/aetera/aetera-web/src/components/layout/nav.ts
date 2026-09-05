import { Puzzle, Sun, type LucideIcon } from "lucide-react";
import { sortByIdOrder } from "@/lib/order";
import { frontendModules } from "@/modules/registry";
import { modulePath } from "@/modules/types";

export interface NavEntry {
  key: string;
  label: string;
  icon: LucideIcon;
  href: string;
  /**
   * 모듈이 뒤를 받치는 메뉴. 사용자가 그 모듈을 켰을 때만 보인다 —
   * 무엇이 보일지는 언제나 서버(`GET /api/v1/me/modules`)가 정한다.
   */
  moduleId?: string;
}

/** 모듈 목록을 위아래에서 감싸는 코어 메뉴. */
const TODAY: NavEntry = { key: "today", label: "오늘", icon: Sun, href: "/dashboard" };
const MODULE_STORE: NavEntry = { key: "modules", label: "모듈", icon: Puzzle, href: "/settings/modules" };

/**
 * 주요 메뉴. 모듈 메뉴는 레지스트리에서 만들어 넣는다 —
 * 여기에 손으로 적으면 모듈 정의와 두 벌이 되어 조용히 어긋난다.
 *
 * 모듈 순서는 사용자가 정한다([orderedModuleIds]). 목록에 없는 모듈은 뒤에 붙여
 * 새로 배포된 모듈이 사라지지 않게 한다.
 */
export function buildMainNav(orderedModuleIds: readonly string[]): NavEntry[] {
  const modules = sortByIdOrder(frontendModules, orderedModuleIds, (module) => module.id);

  return [
    TODAY,
    ...modules.map((module) => ({
      key: module.id,
      label: module.title,
      icon: module.icon,
      href: modulePath(module.id),
      moduleId: module.id,
    })),
    MODULE_STORE,
  ];
}

export function isNavActive(entry: NavEntry, pathname: string): boolean {
  return entry.href === "/dashboard" ? pathname === "/dashboard" : pathname.startsWith(entry.href);
}
