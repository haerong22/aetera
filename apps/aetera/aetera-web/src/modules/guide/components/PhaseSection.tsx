"use client";

import { useId, useState } from "react";
import { Check, ChevronDown } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { cn } from "@/components/ui/cn";
import type { GuidePhase, GuideTask, TaskPatch } from "../api";
import type { TaskToolMap } from "../types";
import { TaskItem } from "./TaskItem";

export function PhaseSection({
  phase,
  order,
  started,
  failedTaskKeys,
  canAddToCalendar,
  taskTools,
  onChangeTask,
  onAddToCalendar,
}: {
  phase: GuidePhase;
  order: number;
  started: boolean;
  failedTaskKeys: ReadonlySet<string>;
  canAddToCalendar: boolean;
  taskTools?: TaskToolMap;
  onChangeTask: (taskKey: string, patch: TaskPatch) => void;
  onAddToCalendar: (task: GuideTask) => void;
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
              onAddToCalendar={
                canAddToCalendar && task.dueDate && !task.done ? () => onAddToCalendar(task) : undefined
              }
              Tool={taskTools?.[task.key]}
              onChange={(patch) => onChangeTask(task.key, patch)}
            />
          ))}
        </ul>
      )}
    </Card>
  );
}
