"use client";

import Link from "next/link";
import { cn } from "@/components/ui/cn";
import type { NavEntry } from "./nav";

/**
 * 사이드바 메뉴 한 줄. href 가 없는 항목(아직 페이지가 없는 기능)은
 * 임시 경로를 만들지 않고 비활성 상태로 표시해 클릭을 막는다.
 */
export function SidebarNavItem({
  entry,
  active,
  onNavigate,
}: {
  entry: NavEntry;
  active: boolean;
  onNavigate?: () => void;
}) {
  const Icon = entry.icon;

  if (!entry.href) {
    return (
      <div
        aria-disabled="true"
        className="flex min-h-11 cursor-default items-center gap-3 rounded-2xl px-4 py-2.5 text-[15px] font-medium text-grey-400 select-none"
      >
        <Icon size={19} strokeWidth={1.8} />
        <span>{entry.label}</span>
        <span className="ml-auto rounded-(--radius-chip) bg-grey-100 px-1.5 py-0.5 text-[10px] font-semibold text-grey-500">
          예정
        </span>
      </div>
    );
  }

  return (
    <Link
      href={entry.href}
      onClick={onNavigate}
      aria-current={active ? "page" : undefined}
      className={cn(
        "flex min-h-11 items-center gap-3 rounded-2xl px-4 py-2.5 text-[15px] font-semibold transition-colors",
        active ? "bg-primary-light text-primary" : "text-grey-600 hover:bg-grey-100",
      )}
    >
      <Icon size={19} strokeWidth={active ? 2.1 : 1.8} />
      <span>{entry.label}</span>
    </Link>
  );
}
