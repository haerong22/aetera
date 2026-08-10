import { Compass, History, Puzzle, Settings, Sparkles, Sun, Target, type LucideIcon } from "lucide-react";
import { frontendModules } from "@/modules/registry";
import { modulePath } from "@/modules/types";

export interface NavEntry {
  key: string;
  label: string;
  icon: LucideIcon;
  /** 없으면 아직 연결된 페이지가 없는 메뉴 — 비활성으로 표시하고 클릭을 막는다. */
  href?: string;
  /**
   * 모듈이 뒤를 받치는 메뉴. 사용자가 그 모듈을 켰을 때만 보인다 —
   * 무엇이 보일지는 언제나 서버(`GET /api/v1/me/modules`)가 정한다.
   */
  moduleId?: string;
}

/**
 * 주요 메뉴. 모듈 메뉴는 레지스트리에서 만들어 넣는다 —
 * 여기에 손으로 적으면 모듈 정의와 두 벌이 되어 조용히 어긋난다.
 * 라우트가 생긴 코어 메뉴는 href 만 채우면 된다.
 */
export const MAIN_NAV: NavEntry[] = [
  { key: "today", label: "오늘", icon: Sun, href: "/dashboard" },
  ...frontendModules.map((module) => ({
    key: module.id,
    label: module.title,
    icon: module.icon,
    href: modulePath(module.id),
    moduleId: module.id,
  })),
  { key: "timeline", label: "타임라인", icon: History },
  { key: "goals", label: "목표", icon: Target },
  { key: "coach", label: "AI 코치", icon: Sparkles },
  { key: "life", label: "라이프", icon: Compass },
  { key: "modules", label: "모듈", icon: Puzzle, href: "/settings/modules" },
];

/** 하단 고정 메뉴. */
export const FOOTER_NAV: NavEntry[] = [{ key: "settings", label: "설정", icon: Settings }];

export function isNavActive(entry: NavEntry, pathname: string): boolean {
  if (!entry.href) return false;
  return entry.href === "/dashboard" ? pathname === "/dashboard" : pathname.startsWith(entry.href);
}
