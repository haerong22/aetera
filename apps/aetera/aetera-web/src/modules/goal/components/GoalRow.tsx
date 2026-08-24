"use client";

import { Minus, Plus } from "lucide-react";
import { ProgressBar } from "@/components/ui/ProgressBar";
import { cn } from "@/components/ui/cn";
import { progressLabel, progressPercent, useAddProgress, type Goal } from "../api";

export function GoalRow({ goal, onEdit }: { goal: Goal; onEdit: () => void }) {
  const addProgress = useAddProgress();
  const percent = progressPercent(goal);

  return (
    <li className="py-4">
      <div className="mb-2 flex flex-wrap items-center justify-between gap-x-3 gap-y-2">
        <button
          type="button"
          onClick={onEdit}
          className="min-w-0 flex-1 truncate text-left text-[15px] font-semibold text-grey-900 hover:underline"
        >
          {goal.title}
        </button>

        <div className="flex shrink-0 items-center gap-2">
          <span
            className={cn(
              "text-[13px] font-semibold tabular-nums",
              goal.achieved ? "text-success" : "text-grey-500",
            )}
          >
            {progressLabel(goal)}
          </span>
          <div className="flex items-center gap-1">
            <button
              type="button"
              aria-label={`${goal.title} 진행 되돌리기`}
              disabled={addProgress.isPending || goal.progress === 0}
              onClick={() => addProgress.mutate({ id: goal.id, amount: -1 })}
              className="flex size-7 items-center justify-center rounded-full text-grey-500 transition-colors hover:bg-grey-100 disabled:text-grey-300 disabled:hover:bg-transparent"
            >
              <Minus size={15} aria-hidden />
            </button>
            <button
              type="button"
              aria-label={`${goal.title} 진행 기록`}
              disabled={addProgress.isPending}
              onClick={() => addProgress.mutate({ id: goal.id, amount: 1 })}
              className="flex size-7 items-center justify-center rounded-full bg-primary-light text-primary transition-colors hover:bg-[#e0e3fc] disabled:opacity-50"
            >
              <Plus size={15} aria-hidden />
            </button>
          </div>
        </div>
      </div>

      <ProgressBar percent={percent} label={goal.title} done={goal.achieved} />
    </li>
  );
}
