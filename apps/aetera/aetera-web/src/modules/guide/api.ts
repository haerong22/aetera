"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";

export interface GuideLink {
  label: string;
  url: string;
}

export interface GuideTask {
  key: string;
  title: string;
  description: string;
  dueOffsetDays: number;
  dueDate?: string;
  required: boolean;
  link?: GuideLink;
  done: boolean;
  note?: string | null;
}

export interface GuidePhase {
  key: string;
  title: string;
  summary: string;
  tasks: GuideTask[];
}

export interface GuideJourney {
  anchorDate: string;
}

export interface GuideProgress {
  total: number;
  done: number;
  requiredTotal: number;
  requiredDone: number;
}

export interface GuideView {
  guideId: string;
  title: string;
  summary: string;
  anchorLabel: string;
  /** `"12-31"`. 달력이 기준일을 정하는 가이드에만 실린다. 연도는 브라우저가 자기 달력으로 맞춘다. */
  anchorMonthDay?: string;
  disclaimer: string;
  journey?: GuideJourney;
  phases: GuidePhase[];
  progress: GuideProgress;
}

const basePath = (guideId: string) => `/api/v1/modules/${guideId}/guide`;

const guideKey = (guideId: string) => [guideId, "guide"] as const;

export function useGuide(guideId: string) {
  return useQuery({
    queryKey: guideKey(guideId),
    queryFn: () => apiFetch<GuideView>(basePath(guideId)),
  });
}

function useGuideMutation<TVariables>(
  guideId: string,
  mutationFn: (variables: TVariables) => Promise<GuideView>,
) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: (view) => queryClient.setQueryData(guideKey(guideId), view),
    onError: () => void queryClient.invalidateQueries({ queryKey: guideKey(guideId) }),
  });
}

export function useSetAnchorDate(guideId: string) {
  return useGuideMutation(guideId, (anchorDate: string) =>
    apiFetch<GuideView>(`${basePath(guideId)}/journey`, {
      method: "PUT",
      body: JSON.stringify({ anchorDate }),
    }),
  );
}

export function useResetJourney(guideId: string) {
  return useGuideMutation(guideId, () =>
    apiFetch<GuideView>(`${basePath(guideId)}/journey`, { method: "DELETE" }),
  );
}

export interface TaskPatch {
  done: boolean;
  note: string | null;
}

export function useUpdateTask(guideId: string) {
  const queryClient = useQueryClient();
  const key = guideKey(guideId);

  return useMutation({
    mutationFn: ({ taskKey, patch }: { taskKey: string; patch: TaskPatch }) =>
      apiFetch<GuideView>(`${basePath(guideId)}/tasks/${taskKey}`, {
        method: "PUT",
        body: JSON.stringify(patch),
      }),
    onMutate: async ({ taskKey, patch }) => {
      await queryClient.cancelQueries({ queryKey: key });
      const previous = queryClient.getQueryData<GuideView>(key);
      if (previous) {
        queryClient.setQueryData(key, applyTaskPatch(previous, taskKey, patch));
      }
      return { previous };
    },
    onError: (_error, _variables, context) => {
      if (context?.previous) queryClient.setQueryData(key, context.previous);
      void queryClient.invalidateQueries({ queryKey: key });
    },
    onSuccess: (view) => queryClient.setQueryData(key, view),
  });
}

function applyTaskPatch(view: GuideView, taskKey: string, patch: TaskPatch): GuideView {
  const phases = view.phases.map((phase) => ({
    ...phase,
    tasks: phase.tasks.map((task) => (task.key === taskKey ? { ...task, ...patch } : task)),
  }));
  const tasks = phases.flatMap((phase) => phase.tasks);
  const doneTasks = tasks.filter((task) => task.done);

  return {
    ...view,
    phases,
    progress: {
      total: tasks.length,
      done: doneTasks.length,
      requiredTotal: tasks.filter((task) => task.required).length,
      requiredDone: doneTasks.filter((task) => task.required).length,
    },
  };
}
