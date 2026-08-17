"use client";

import { useState } from "react";
import { Info } from "lucide-react";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { ModuleDisabledNotice, isModuleDisabled } from "../ModuleDisabledNotice";
import type { CalendarDraft } from "../types";
import { useAddEventDialog } from "../capabilityRegistry";
import {
  useGuide,
  useResetJourney,
  useSetAnchorDate,
  useUpdateTask,
  type GuideTask,
  type TaskPatch,
} from "./api";
import { JourneyHeader } from "./components/JourneyHeader";
import { PhaseSection } from "./components/PhaseSection";
import { StartJourneyCard } from "./components/StartJourneyCard";

export function GuidePage({ moduleTitle, guideId }: { moduleTitle: string; guideId: string }) {
  const { data: guide, error, refetch } = useGuide(guideId);
  const setAnchorDate = useSetAnchorDate(guideId);
  const resetJourney = useResetJourney(guideId);
  const updateTask = useUpdateTask(guideId);
  const AddEventDialog = useAddEventDialog();

  const [failedTaskKeys, setFailedTaskKeys] = useState<ReadonlySet<string>>(new Set());
  const [calendarDraft, setCalendarDraft] = useState<CalendarDraft | null>(null);

  function markTaskFailed(taskKey: string, failed: boolean) {
    setFailedTaskKeys((previous) => {
      if (previous.has(taskKey) === failed) return previous;
      const next = new Set(previous);
      if (failed) next.add(taskKey);
      else next.delete(taskKey);
      return next;
    });
  }

  function clearTaskFeedback() {
    setFailedTaskKeys((previous) => (previous.size === 0 ? previous : new Set()));
  }

  function changeTask(taskKey: string, patch: TaskPatch) {
    markTaskFailed(taskKey, false);
    updateTask.mutate({ taskKey, patch }, { onError: () => markTaskFailed(taskKey, true) });
  }

  function addTaskToCalendar(task: GuideTask) {
    if (!task.dueDate) return;
    setCalendarDraft({
      title: `[${moduleTitle}] ${task.title}`,
      description: task.description,
      date: task.dueDate,
    });
  }

  if (error && isModuleDisabled(error)) return <ModuleDisabledNotice title={moduleTitle} />;
  if (!guide) return error ? <ErrorState onRetry={() => void refetch()} /> : <PageSpinner />;

  const started = Boolean(guide.journey);

  return (
    <div className="flex max-w-3xl flex-col gap-5">
      <div>
        <h1 className="text-2xl font-bold text-grey-900">{guide.title}</h1>
        <p className="mt-1 text-[15px] text-grey-500">{guide.summary}</p>
      </div>

      {guide.journey ? (
        <JourneyHeader
          anchorLabel={guide.anchorLabel}
          journey={guide.journey}
          progress={guide.progress}
          changePending={setAnchorDate.isPending}
          changeFailed={setAnchorDate.isError}
          resetPending={resetJourney.isPending}
          resetFailed={resetJourney.isError}
          onChangeAnchorDate={(anchorDate, onDone) => setAnchorDate.mutate(anchorDate, { onSuccess: onDone })}
          onReset={() => resetJourney.mutate(undefined, { onSuccess: clearTaskFeedback })}
        />
      ) : (
        <StartJourneyCard
          anchorLabel={guide.anchorLabel}
          taskCount={guide.progress.total}
          pending={setAnchorDate.isPending}
          failed={setAnchorDate.isError}
          onStart={(anchorDate) => setAnchorDate.mutate(anchorDate)}
        />
      )}

      <div className="flex flex-col gap-4">
        {guide.phases.map((phase, index) => (
          <PhaseSection
            key={phase.key}
            phase={phase}
            order={index + 1}
            started={started}
            failedTaskKeys={failedTaskKeys}
            canAddToCalendar={started && AddEventDialog !== null}
            onChangeTask={changeTask}
            onAddToCalendar={addTaskToCalendar}
          />
        ))}
      </div>

      <p className="flex items-start gap-2 rounded-(--radius-card) bg-grey-100 px-4 py-3 text-[13px] leading-relaxed text-grey-500">
        <Info size={15} aria-hidden className="mt-0.5 shrink-0" />
        {guide.disclaimer}
      </p>

      {AddEventDialog && calendarDraft && (
        <AddEventDialog open onClose={() => setCalendarDraft(null)} draft={calendarDraft} />
      )}
    </div>
  );
}
