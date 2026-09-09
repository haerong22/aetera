"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";

export interface TimelineEntry {
  /** 어느 모듈이 낸 줄인지. 아이콘과 이름을 레지스트리에서 이 값으로 찾는다. */
  moduleId: string;
  on: string;
  title: string;
  detail?: string;
}

/**
 * 기간 안의 줄들. 최근 것이 위다.
 *
 * 해를 넘길 때마다 queryKey 가 바뀌므로 [keepPreviousData] 를 건다. 없으면 새 해를 받아오는
 * 동안 `data` 가 비어 **연도 버튼까지 사라진다** — 방금 누른 버튼이 손 밑에서 없어지는 셈이다.
 */
export function useTimeline(from: string, to: string) {
  return useQuery({
    queryKey: ["timeline", "entries", from, to] as const,
    queryFn: () =>
      apiFetch<TimelineEntry[]>(`/api/v1/modules/timeline/entries?from=${from}&to=${to}`),
    placeholderData: keepPreviousData,
  });
}
