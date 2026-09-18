"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";

export interface NotificationPreference {
  enabled: boolean;
  /** 받을 시각(0~23). 내 시간대 기준이다. */
  sendHour: number;
}

const BASE = "/api/v1/me/notifications";
const KEY = ["me", "notifications"] as const;

/**
 * 모듈이 아니라 코어 기능이라 `modules/` 가 아닌 여기 있다.
 * 주소도 `/api/v1/me` 아래다 — `/modules/..` 에 두면 코어의 활성화 가드가 막는다.
 */
export function useNotificationPreference() {
  return useQuery({
    queryKey: KEY,
    queryFn: () => apiFetch<NotificationPreference>(BASE),
  });
}

export function useChangeNotificationPreference() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (preference: NotificationPreference) =>
      apiFetch<NotificationPreference>(BASE, { method: "PUT", body: JSON.stringify(preference) }),
    onSuccess: (saved) => queryClient.setQueryData(KEY, saved),
    onError: () => void queryClient.invalidateQueries({ queryKey: KEY }),
  });
}
