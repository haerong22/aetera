"use client";

import { useMutation } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";

const BASE = "/api/v1/auth/password-reset";

/**
 * 재설정 메일 요청.
 *
 * **성공해도 가입한 주소인지는 알 수 없다.** 서버가 일부러 구분하지 않는다 — 구분해 주면
 * 이 화면이 가입 여부 조회기가 된다. 그래서 화면도 "보냈다"가 아니라 "가입돼 있다면
 * 보냈다"로 말해야 한다.
 */
export function useRequestPasswordReset() {
  return useMutation({
    mutationFn: (email: string) =>
      apiFetch<void>(BASE, { method: "POST", body: JSON.stringify({ email }) }),
  });
}

export interface ResetPasswordInput {
  token: string;
  newPassword: string;
}

/** 링크로 받은 토큰으로 다시 정하기. 세션은 주지 않는다 — 끝나면 로그인 화면으로 보낸다. */
export function useResetPassword() {
  return useMutation({
    mutationFn: (input: ResetPasswordInput) =>
      apiFetch<void>(`${BASE}/confirm`, { method: "POST", body: JSON.stringify(input) }),
  });
}
