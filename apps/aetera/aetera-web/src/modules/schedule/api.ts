"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";

export interface ScheduleEvent {
  id: string;
  title: string;
  description?: string;
  startsAt: string;
  endsAt: string;
  allDay: boolean;
  color?: string;
  createdAt: string;
}

export interface ScheduleEventInput {
  title: string;
  description?: string;
  startsAt: string;
  endsAt: string;
  allDay: boolean;
  color?: string;
}

const BASE = "/api/v1/modules/schedule/events";

const eventsKey = (from: string, to: string) => ["schedule", "events", from, to] as const;

export function useScheduleEvents(
  from: Date,
  to: Date,
  options?: { enabled?: boolean },
) {
  const fromIso = from.toISOString();
  const toIso = to.toISOString();
  return useQuery({
    queryKey: eventsKey(fromIso, toIso),
    queryFn: () =>
      apiFetch<ScheduleEvent[]>(
        `${BASE}?from=${encodeURIComponent(fromIso)}&to=${encodeURIComponent(toIso)}`,
      ),
    enabled: options?.enabled ?? true,
  });
}

function useInvalidateEvents() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ["schedule", "events"] });
}

export function useCreateEvent() {
  const invalidate = useInvalidateEvents();
  return useMutation({
    mutationFn: (input: ScheduleEventInput) =>
      apiFetch<ScheduleEvent>(BASE, { method: "POST", body: JSON.stringify(input) }),
    onSuccess: invalidate,
  });
}

export function useUpdateEvent() {
  const invalidate = useInvalidateEvents();
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: ScheduleEventInput }) =>
      apiFetch<ScheduleEvent>(`${BASE}/${id}`, { method: "PUT", body: JSON.stringify(input) }),
    onSuccess: invalidate,
  });
}

export function useDeleteEvent() {
  const invalidate = useInvalidateEvents();
  return useMutation({
    mutationFn: (id: string) => apiFetch<void>(`${BASE}/${id}`, { method: "DELETE" }),
    onSuccess: invalidate,
  });
}
