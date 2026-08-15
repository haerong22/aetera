"use client";

import { useId, useState } from "react";
import { Check, ChevronDown } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { cn } from "@/components/ui/cn";
import type { GuidePhase, TaskPatch } from "../api";
import { TaskItem } from "./TaskItem";

/**
 * 단계 하나. 다 끝낸 단계는 접힌 채로 시작한다 —
 * 27개를 한 화면에 펼쳐 두면 지금 할 일이 어디 있는지 보이지 않는다.
 */
export function PhaseSection({
  phase,
  order,
  started,
  failedTaskKeys,
  onChangeTask,
}: {
  phase: GuidePhase;
  order: number;
  started: boolean;
  /** 저장에 실패해 아직 반영되지 않은 항목. 그 항목 옆에 안내를 붙인다. */
  failedTaskKeys: ReadonlySet<string>;
  onChangeTask: (taskKey: string, patch: TaskPatch) => void;
}) {
  const doneCount = phase.tasks.filter((task) => task.done).length;
  const completed = doneCount === phase.tasks.length;
  const [open, setOpen] = useState(!completed);
  const panelId = useId();

  return (
    <Card className="p-0 sm:p-0">
      <button
        type="button"
        aria-expanded={open}
        aria-controls={panelId}
        onClick={() => setOpen((previous) => !previous)}
        className="flex w-full items-center gap-3 rounded-(--radius-card) px-5 py-4 text-left transition-colors hover:bg-grey-50 sm:px-6"
      >
        <span
          className={cn(
            "flex size-8 shrink-0 items-center justify-center rounded-xl text-[13px] font-bold",
            completed ? "bg-success-light text-success" : "bg-primary-light text-primary",
          )}
        >
          {completed ? <Check size={16} aria-hidden /> : order}
        </span>

        <span className="min-w-0 flex-1">
          <span className="flex items-center gap-2">
            <span className="truncate text-[16px] font-bold text-grey-900">{phase.title}</span>
            <span className="shrink-0 text-[13px] font-semibold text-grey-400 tabular-nums">
              {doneCount}/{phase.tasks.length}
            </span>
          </span>
          <span className="mt-0.5 block text-[13px] leading-relaxed text-grey-500">{phase.summary}</span>
        </span>

        <ChevronDown
          size={18}
          aria-hidden
          className={cn("shrink-0 text-grey-400 transition-transform duration-200", open && "rotate-180")}
        />
      </button>

      {open && (
        <ul id={panelId} className="divide-y divide-grey-100 border-t border-grey-100 px-5 sm:px-6">
          {phase.tasks.map((task) => (
            <TaskItem
              key={task.key}
              task={task}
              started={started}
              failed={failedTaskKeys.has(task.key)}
              onChange={(patch) => onChangeTask(task.key, patch)}
            />
          ))}
        </ul>
      )}
    </Card>
  );
}
