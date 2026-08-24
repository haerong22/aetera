"use client";

import Link from "next/link";
import { ArrowRight, Puzzle, Target } from "lucide-react";
import { Card, CardHeader } from "@/components/ui/Card";
import { ProgressBar } from "@/components/ui/ProgressBar";
import { Spinner } from "@/components/ui/Spinner";
import { cn } from "@/components/ui/cn";
import { progressLabel, progressPercent, useGoals } from "@/modules/goal/api";
import { GOAL_MODULE_ID } from "@/modules/goal/id";
import { modulePath } from "@/modules/types";

export function WeeklyGoalsCard({ enabled }: { enabled: boolean }) {
  const { data: goals, isLoading } = useGoals({ enabled });

  if (!enabled) {
    return (
      <Card className="flex h-full flex-col items-center justify-center gap-3 py-10 text-center">
        <span className="flex size-11 items-center justify-center rounded-2xl bg-primary-light text-primary">
          <Puzzle size={20} aria-hidden />
        </span>
        <p className="text-[15px] font-semibold text-grey-800">목표 모듈을 켜면 이번 주 목표가 여기 표시돼요</p>
        <Link href="/settings/modules" className="text-[14px] font-semibold text-primary hover:underline">
          모듈 살펴보기
        </Link>
      </Card>
    );
  }

  const weekly = goals?.filter((goal) => goal.period === "WEEKLY") ?? [];

  return (
    <Card className="h-full">
      <CardHeader
        icon={<Target size={16} />}
        title="이번 주 목표"
        action={
          <Link
            href={modulePath(GOAL_MODULE_ID)}
            className="flex items-center gap-0.5 text-[13px] font-semibold text-primary hover:underline"
          >
            전체 보기 <ArrowRight size={14} aria-hidden />
          </Link>
        }
      />

      {isLoading ? (
        <div className="flex justify-center py-8">
          <Spinner />
        </div>
      ) : weekly.length === 0 ? (
        <div className="py-7 text-center">
          <p className="text-[15px] font-medium text-grey-700">이번 주 목표가 없어요.</p>
          <p className="mt-1 text-[13px] text-grey-500">작게 하나만 정해도 한 주가 달라져요.</p>
        </div>
      ) : (
        <ul className="flex flex-col gap-4">
          {weekly.map((goal) => {
            const percent = progressPercent(goal);
            return (
              <li key={goal.id}>
                <div className="mb-1.5 flex items-baseline justify-between gap-2">
                  <span className="min-w-0 truncate text-[14px] font-medium text-grey-800">{goal.title}</span>
                  <span
                    className={cn(
                      "shrink-0 text-[13px] font-semibold tabular-nums",
                      goal.achieved ? "text-success" : "text-grey-500",
                    )}
                  >
                    {progressLabel(goal)}
                  </span>
                </div>
                <ProgressBar percent={percent} label={goal.title} done={goal.achieved} />
              </li>
            );
          })}
        </ul>
      )}
    </Card>
  );
}
