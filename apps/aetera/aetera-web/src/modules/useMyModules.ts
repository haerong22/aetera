"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";
import { moduleById } from "@/modules/registry";
import type { ModuleSummary } from "@/lib/types";

const MY_MODULES_KEY = ["me", "modules"] as const;

export function useMyModules() {
  return useQuery({
    queryKey: MY_MODULES_KEY,
    queryFn: () => apiFetch<ModuleSummary[]>("/api/v1/me/modules"),
  });
}

export function useToggleModule() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ moduleId, enable }: { moduleId: string; enable: boolean }) =>
      apiFetch<ModuleSummary>(`/api/v1/me/modules/${moduleId}/enablement`, {
        method: enable ? "POST" : "DELETE",
      }),
    onSuccess: (updated) => {
      queryClient.setQueryData<ModuleSummary[]>(MY_MODULES_KEY, (previous) =>
        previous?.map((module) => (module.id === updated.id ? updated : module)),
      );
      // 모듈을 껐다 켜면 그 모듈이 캐시에 남긴 데이터(및 403 이던 시절의 상태)를 버린다.
      // 접두사는 모듈이 계약으로 알려 준다 — 코어가 키 규칙을 짐작하지 않는다.
      const prefix = moduleById.get(updated.id)?.queryKeyPrefix;
      if (prefix) queryClient.removeQueries({ queryKey: [prefix] });
    },
  });
}
