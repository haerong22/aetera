"use client";

import {
  Briefcase,
  Compass,
  HeartPulse,
  Sprout,
  Users,
  Wallet,
  type LucideIcon,
} from "lucide-react";
import { Card, CardHeader } from "@/components/ui/Card";
import { cn } from "@/components/ui/cn";
import { MOCK_LIFE_AREAS, type LifeAreaKey, type MockLifeArea } from "./mock";

const AREA_ICONS: Record<LifeAreaKey, LucideIcon> = {
  health: HeartPulse,
  career: Briefcase,
  relationship: Users,
  finance: Wallet,
  growth: Sprout,
};

const TONE_DOT: Record<MockLifeArea["tone"], string> = {
  success: "bg-success",
  warning: "bg-warning",
  muted: "bg-grey-300",
};

/**
 * 나의 라이프 — 영역별 현재 상태.
 * 데이터가 아직 없으므로 점수 대신 "기록 필요" 같은 상태 텍스트만 보여준다.
 * 근거 없는 수치를 확정된 분석처럼 표시하지 않는 것이 원칙.
 */
export function LifeOverviewCard() {
  return (
    <Card className="h-full">
      <CardHeader icon={<Compass size={16} />} title="나의 라이프" />

      <ul className="flex flex-col">
        {MOCK_LIFE_AREAS.map((area) => {
          const Icon = AREA_ICONS[area.key];
          return (
            <li
              key={area.key}
              className="flex min-h-11 items-center gap-3 border-b border-grey-100 px-1 py-2.5 last:border-b-0"
            >
              <span className="flex size-8 shrink-0 items-center justify-center rounded-xl bg-grey-100 text-grey-600">
                <Icon size={16} strokeWidth={1.9} />
              </span>
              <span className="min-w-0 flex-1 text-[15px] font-medium text-grey-800">{area.label}</span>
              <span className="flex items-center gap-1.5 text-[13px] font-medium text-grey-500">
                <span className={cn("size-1.5 rounded-full", TONE_DOT[area.tone])} aria-hidden="true" />
                {area.status}
              </span>
            </li>
          );
        })}
      </ul>

      <p className="mt-3 px-1 text-[12px] text-grey-400">
        기록과 모듈이 쌓이면 영역별 상태가 여기서 정리돼요.
      </p>
    </Card>
  );
}
