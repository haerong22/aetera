"use client";

import Link from "next/link";
import { cn } from "@/components/ui/cn";
import type { NavEntry } from "./nav";

/** 사이드바 메뉴 한 줄. 모든 항목이 갈 곳을 갖는다 — 눌리지 않는 자리는 두지 않는다. */
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
