"use client";

import { useMutation } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";
import type { User } from "@/lib/types";

export interface ChangePasswordInput {
  currentPassword: string;
  newPassword: string;
}

/** 서버가 새 프로필을 돌려준다 — 다듬은 닉네임이 그 안에 있으므로 앱은 그것을 그대로 쓴다. */
export function useChangeNickname() {
  return useMutation({
    mutationFn: (nickname: string) =>
      apiFetch<User>("/api/v1/me", { method: "PUT", body: JSON.stringify({ nickname }) }),
  });
}

/**
 * 비밀번호 변경.
 *
 * 돌려받는 것이 없다(204). 대신 **새 리프레시 쿠키가 응답에 실려 온다** — 서버가 다른
 * 기기를 모두 끊으면서 이 기기 몫만 새로 발급하기 때문이다. 쿠키는 브라우저가 알아서
 * 갈아 끼우므로 여기서 할 일이 없고, 메모리의 액세스 토큰도 만료까지는 그대로 쓸 수 있다.
 */
export function useChangePassword() {
  return useMutation({
    mutationFn: (input: ChangePasswordInput) =>
      apiFetch<void>("/api/v1/me/password", { method: "PUT", body: JSON.stringify(input) }),
  });
}
