"use client";

import Link from "next/link";
import { Puzzle } from "lucide-react";
import { StatusCard } from "@/components/ui/StatusCard";
import { ApiError } from "@/lib/api-client";

/**
 * 백엔드 모듈 가드의 403(FORBIDDEN) — 아직 이 모듈을 활성화하지 않았다는 뜻.
 *
 * 모듈마다 이 판별을 다시 적으면 하나가 조용히 달라진다(예: 404 까지 같이 처리해서
 * 진짜 없는 리소스를 "모듈을 켜세요"로 안내하는 식). 판별과 안내를 한곳에 둔다.
 */
export function isModuleDisabled(error: unknown): boolean {
  return error instanceof ApiError && error.status === 403;
}

/** 켜지 않은 모듈에 들어왔을 때의 안내. 모든 모듈이 같은 문구·같은 동선을 쓴다. */
export function ModuleDisabledNotice({ title }: { title: string }) {
  return (
    <StatusCard
      tone="primary"
      icon={<Puzzle size={22} aria-hidden />}
      title={`${title} 모듈을 아직 사용하고 있지 않아요`}
      description="모듈 스토어에서 켜면 바로 쓸 수 있어요."
      action={
        <Link
          href="/settings/modules"
          className="rounded-(--radius-button) bg-primary px-5 py-3 text-[15px] font-semibold text-white transition-colors hover:bg-primary-hover"
        >
          모듈 스토어로 가기
        </Link>
      }
    />
  );
}
