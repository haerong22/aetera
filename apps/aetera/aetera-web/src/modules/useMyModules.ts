"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";
import { sortByIdOrder } from "@/lib/order";
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

/**
 * 사이드바 순서를 바꾼다.
 *
 * 화면이 이미 계산한 최종 순서를 통째로 보낸다 — "한 칸 위로" 같은 부분 요청으로 두면
 * 서버가 나머지를 어떻게 밀지 정해야 하고, 그 규칙이 화면과 어긋나면 순서가 튄다.
 *
 * 순서는 즉시 반영한다. 화살표를 눌렀는데 목록이 안 움직이면 눌리지 않은 줄 안다.
 */
export function useReorderModules() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (moduleIds: string[]) =>
      apiFetch<ModuleSummary[]>("/api/v1/me/modules/order", {
        method: "PUT",
        body: JSON.stringify({ moduleIds }),
      }),
    onMutate: async (moduleIds) => {
      await queryClient.cancelQueries({ queryKey: MY_MODULES_KEY });
      const previous = queryClient.getQueryData<ModuleSummary[]>(MY_MODULES_KEY);
      if (previous) {
        queryClient.setQueryData<ModuleSummary[]>(
          MY_MODULES_KEY,
          sortByIdOrder(previous, moduleIds, (module) => module.id),
        );
      }
      return { previous };
    },
    onError: (_error, _variables, context) => {
      if (context?.previous) queryClient.setQueryData(MY_MODULES_KEY, context.previous);
    },
    onSuccess: (updated) => queryClient.setQueryData(MY_MODULES_KEY, updated),
  });
}
