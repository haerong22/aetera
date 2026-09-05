"use client";

import { useEffect, useState, type ReactNode } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { LogOut, Menu, X } from "lucide-react";
import { useAuth } from "@/lib/auth";
import { useMyModules } from "@/modules/useMyModules";
import { buildMainNav, isNavActive } from "@/components/layout/nav";
import { SidebarNavItem } from "@/components/layout/SidebarNavItem";
import { PageSpinner } from "@/components/ui/Spinner";

function BrandLogo() {
  return (
    <span className="text-[22px] font-extrabold tracking-tight text-grey-900">
      ae<span className="text-primary">tera</span>
    </span>
  );
}

function SidebarContent({ onNavigate }: { onNavigate?: () => void }) {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const router = useRouter();
  const { data: modules } = useMyModules();

  // 모듈이 받치는 메뉴는 그 모듈을 켠 사용자에게만 보인다.
  // 모듈 목록을 아직 못 받았으면 성급히 숨기지 않는다(깜빡임 방지) — 접근 차단은 서버 가드의 몫이다.
  const mainNav = buildMainNav(modules?.map((module) => module.id) ?? []);
  const visibleNav = mainNav.filter((entry) => {
    if (!entry.moduleId || !modules) return true;
    return modules.some((module) => module.id === entry.moduleId && module.enabled);
  });

  return (
    <div className="flex h-full flex-col gap-1 px-4 py-7">
      <Link href="/dashboard" onClick={onNavigate} className="mb-7 px-4">
        <BrandLogo />
      </Link>

      <nav aria-label="주요 메뉴" className="flex flex-col gap-0.5">
        {visibleNav.map((entry) => (
          <SidebarNavItem
            key={entry.key}
            entry={entry}
            active={isNavActive(entry, pathname)}
            onNavigate={onNavigate}
          />
        ))}
      </nav>

      <div className="mt-auto flex flex-col gap-0.5 border-t border-grey-100 pt-3">
        <div className="flex items-center justify-between px-4 pt-3">
          <div className="min-w-0">
            <p className="truncate text-[14px] font-semibold text-grey-800">{user?.nickname}</p>
            <p className="truncate text-[12px] text-grey-500">{user?.email}</p>
          </div>
          <button
            aria-label="로그아웃"
            onClick={() => logout().then(() => router.replace("/login"))}
            className="shrink-0 rounded-full p-2.5 text-grey-500 transition-colors hover:bg-grey-100"
          >
            <LogOut size={18} />
          </button>
        </div>
      </div>
    </div>
  );
}

export default function PlatformLayout({ children }: { children: ReactNode }) {
  const { status } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const [drawerOpen, setDrawerOpen] = useState(false);

  useEffect(() => {
    if (status === "guest") router.replace("/login");
  }, [status, router]);

  // 라우트가 바뀌면 모바일 drawer 를 닫는다.
  useEffect(() => {
    setDrawerOpen(false);
  }, [pathname]);

  useEffect(() => {
    if (!drawerOpen) return;
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") setDrawerOpen(false);
    };
    document.addEventListener("keydown", onKeyDown);
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKeyDown);
      document.body.style.overflow = "";
    };
  }, [drawerOpen]);

  if (status !== "authenticated") return <PageSpinner />;

  return (
    <div className="mx-auto flex min-h-dvh w-full max-w-[1440px]">
      {/* 데스크톱/태블릿 사이드바 */}
      <aside className="sticky top-0 hidden h-dvh w-60 shrink-0 border-r border-grey-200 bg-white md:block">
        <SidebarContent />
      </aside>

      {/* 모바일 drawer */}
      {drawerOpen && (
        <div className="fixed inset-0 z-50 md:hidden" role="dialog" aria-modal="true" aria-label="메뉴">
          <button
            aria-label="메뉴 닫기"
            className="absolute inset-0 bg-grey-900/40 backdrop-blur-[2px]"
            onClick={() => setDrawerOpen(false)}
          />
          <div className="absolute inset-y-0 left-0 w-72 max-w-[85vw] bg-white shadow-xl">
            <button
              aria-label="메뉴 닫기"
              onClick={() => setDrawerOpen(false)}
              className="absolute top-5 right-4 rounded-full p-2 text-grey-500 hover:bg-grey-100"
            >
              <X size={20} />
            </button>
            <SidebarContent onNavigate={() => setDrawerOpen(false)} />
          </div>
        </div>
      )}

      <div className="flex min-w-0 flex-1 flex-col">
        {/* 모바일 상단바 */}
        <header className="sticky top-0 z-40 flex items-center justify-between border-b border-grey-200 bg-white/90 px-4 py-3 backdrop-blur md:hidden">
          <button
            aria-label="메뉴 열기"
            aria-expanded={drawerOpen}
            onClick={() => setDrawerOpen(true)}
            className="rounded-xl p-2.5 text-grey-700 hover:bg-grey-100"
          >
            <Menu size={22} />
          </button>
          <Link href="/dashboard" aria-label="오늘로 이동">
            <BrandLogo />
          </Link>
          <span className="w-10" aria-hidden="true" />
        </header>

        <main className="flex-1 px-4 py-6 sm:px-6 sm:py-8 lg:px-10">
          <div className="mx-auto w-full max-w-[1160px]">{children}</div>
        </main>
      </div>
    </div>
  );
}
