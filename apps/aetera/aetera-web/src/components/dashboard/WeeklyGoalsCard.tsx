"use client";

import { Target } from "lucide-react";
import { Badge } from "@/components/ui/Badge";
import { Card, CardHeader } from "@/components/ui/Card";
import { MOCK_GOALS } from "./mock";

/** 이번 주 목표 진행률. 목표 모듈이 아직 없어 mock 데이터로 UI 만 구성한다. */
export function WeeklyGoalsCard() {
  return (
    <Card className="h-full">
      <CardHeader
        icon={<Target size={16} />}
        title="이번 주 목표"
        action={<Badge tone="grey">미리보기</Badge>}
      />

      <ul className="flex flex-col gap-4">
        {MOCK_GOALS.map((goal) => {
          const percent = Math.round((goal.current / goal.target) * 100);
          return (
            <li key={goal.id}>
              <div className="mb-1.5 flex items-baseline justify-between gap-2">
                <span className="min-w-0 truncate text-[14px] font-medium text-grey-800">{goal.title}</span>
                <span className="shrink-0 text-[13px] font-semibold tabular-nums text-grey-500">
                  {goal.progressLabel}
                </span>
              </div>
              <div
                role="progressbar"
                aria-valuenow={percent}
                aria-valuemin={0}
                aria-valuemax={100}
                aria-label={`${goal.title} 진행률 ${percent}%`}
                className="h-1.5 overflow-hidden rounded-full bg-grey-100"
              >
                <div
                  className="h-full rounded-full bg-primary transition-[width] duration-300"
                  style={{ width: `${percent}%` }}
                />
              </div>
            </li>
          );
        })}
      </ul>

      <p className="mt-4 text-[12px] text-grey-400">목표 모듈이 열리면 실제 목표와 연결돼요.</p>
    </Card>
  );
}
