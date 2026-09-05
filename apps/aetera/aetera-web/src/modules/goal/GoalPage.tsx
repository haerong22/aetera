"use client";

import { useState } from "react";
import { Plus, Target } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/StatusCard";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { ModuleDisabledNotice, isModuleDisabled } from "../ModuleDisabledNotice";
import { PERIOD_LABELS, useGoals, type Goal, type GoalPeriod } from "./api";
import { GoalDialog } from "./components/GoalDialog";
import { GoalRow } from "./components/GoalRow";

const PERIOD_ORDER: GoalPeriod[] = ["WEEKLY", "MONTHLY"];

export function GoalPage() {
  const { data: goals, error, refetch } = useGoals();
  const [editing, setEditing] = useState<Goal | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);

  if (error && isModuleDisabled(error)) return <ModuleDisabledNotice title="목표" />;
  if (!goals) return error ? <ErrorState onRetry={() => void refetch()} /> : <PageSpinner />;

  const achieved = goals.filter((goal) => goal.achieved).length;

  return (
    <div className="flex max-w-3xl flex-col gap-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-grey-900">목표</h1>
          <p className="mt-1 text-[15px] text-grey-500">
            {goals.length === 0
              ? "이번 주에 뭘 하기로 했는지 정해 보세요."
              : `${goals.length}개 중 ${achieved}개를 이뤘어요.`}
          </p>
        </div>
        <Button
          onClick={() => {
            setEditing(null);
            setDialogOpen(true);
          }}
        >
          <Plus size={17} aria-hidden /> 목표 추가
        </Button>
      </div>

      {goals.length === 0 ? (
        <EmptyState
          icon={<Target size={22} aria-hidden />}
          title="아직 정한 목표가 없어요"
          description={'"주 3회 운동"처럼 작게 시작하면 지키기 쉬워요.'}
        />
      ) : (
        PERIOD_ORDER.map((period) => {
          const inPeriod = goals.filter((goal) => goal.period === period);
          if (inPeriod.length === 0) return null;

          return (
            <Card key={period} className="p-0 sm:p-0">
              <p className="border-b border-grey-100 px-5 py-3 text-[13px] font-semibold text-grey-500 sm:px-6">
                {PERIOD_LABELS[period]}
              </p>
              <ul className="divide-y divide-grey-100 px-5 sm:px-6">
                {inPeriod.map((goal) => (
                  <GoalRow
                    key={goal.id}
                    goal={goal}
                    onEdit={() => {
                      setEditing(goal);
                      setDialogOpen(true);
                    }}
                  />
                ))}
              </ul>
            </Card>
          );
        })
      )}

      <GoalDialog open={dialogOpen} onClose={() => setDialogOpen(false)} goal={editing} />
    </div>
  );
}
