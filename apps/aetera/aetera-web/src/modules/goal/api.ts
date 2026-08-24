"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";

export type GoalPeriod = "WEEKLY" | "MONTHLY";

export interface Goal {
  id: string;
  title: string;
  period: GoalPeriod;
  target: number;
  unit?: string;
  progress: number;
  periodStart: string;
  achieved: boolean;
}

interface GoalInput {
  title: string;
  period: GoalPeriod;
  target: number;
  unit?: string;
}

const BASE = "/api/v1/modules/goal/goals";
const GOALS_KEY = ["goal", "goals"] as const;

export function useGoals(options?: { enabled?: boolean }) {
  return useQuery({
    queryKey: GOALS_KEY,
    queryFn: () => apiFetch<Goal[]>(BASE),
    enabled: options?.enabled ?? true,
  });
}

function useGoalMutation<TVariables>(mutationFn: (variables: TVariables) => Promise<unknown>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: GOALS_KEY }),
  });
}

export function useCreateGoal() {
  return useGoalMutation((input: GoalInput) =>
    apiFetch<Goal>(BASE, { method: "POST", body: JSON.stringify(input) }),
  );
}

export function useUpdateGoal() {
  return useGoalMutation(({ id, input }: { id: string; input: GoalInput }) =>
    apiFetch<Goal>(`${BASE}/${id}`, { method: "PUT", body: JSON.stringify(input) }),
  );
}

export function useAddProgress() {
  return useGoalMutation(({ id, amount }: { id: string; amount: number }) =>
    apiFetch<Goal>(`${BASE}/${id}/progress`, { method: "POST", body: JSON.stringify({ amount }) }),
  );
}

export function useDeleteGoal() {
  return useGoalMutation((id: string) => apiFetch<void>(`${BASE}/${id}`, { method: "DELETE" }));
}

export const PERIOD_LABELS: Record<GoalPeriod, string> = {
  WEEKLY: "이번 주",
  MONTHLY: "이번 달",
};

export function progressPercent(goal: Goal): number {
  return Math.min(100, Math.round((goal.progress / goal.target) * 100));
}

export function progressLabel(goal: Goal): string {
  return `${goal.progress}/${goal.target}${goal.unit ?? ""}`;
}
