"use client";

import type { FormEvent } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { NewPasswordFields, useNewPassword } from "@/components/auth/NewPasswordFields";
import { Button } from "@/components/ui/Button";
import { Spinner } from "@/components/ui/Spinner";
import { errorMessage } from "@/lib/api-client";
import { useResetPassword } from "@/lib/passwordReset";

/**
 * 링크를 열고 새 비밀번호를 정하는 화면.
 *
 * 끝나도 **로그인시키지 않는다.** 여기까지 온 사람이 주인이라는 근거는 메일함을 열었다는
 * 것뿐이고, 서버도 세션을 주지 않는다. 방금 정한 비밀번호를 한 번 손으로 넣어 보는 것이
 * 오타를 마지막으로 거르는 자리이기도 하다.
 */
export function ResetPasswordForm() {
  const router = useRouter();
  const token = useSearchParams().get("token") ?? "";
  const password = useNewPassword();
  const reset = useResetPassword();

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (!password.ready) return;
    reset.mutate({ token, newPassword: password.value });
  }

  // 링크에 토큰이 없으면 보낼 것이 없다. 빈 칸을 먼저 보여 주고 누른 뒤에 실패시키지 않는다.
  if (!token) {
    return (
      <Failed>
        주소에 링크 정보가 없어요. 메일에 있는 주소를 그대로 열어 주세요.
      </Failed>
    );
  }

  if (reset.isSuccess) {
    return (
      <div>
        <h1 className="text-[26px] font-bold leading-snug text-grey-900">비밀번호를 바꿨어요</h1>
        <p className="mt-3 text-[15px] leading-relaxed text-grey-500">
          열려 있던 기기는 모두 로그아웃됐어요. 새 비밀번호로 다시 로그인해 주세요.
        </p>
        <Button size="lg" className="mt-8 w-full" onClick={() => router.replace("/login")}>
          로그인하러 가기
        </Button>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-[26px] font-bold leading-snug text-grey-900">새 비밀번호를 정해 주세요</h1>

      <form onSubmit={onSubmit} className="mt-10 flex flex-col gap-4">
        <NewPasswordFields password={password} disabled={reset.isPending} autoFocus />

        {reset.isError && (
          <p
            role="alert"
            className="rounded-xl bg-danger-light px-4 py-3 text-[13px] font-medium text-danger"
          >
            {errorMessage(reset.error, "바꾸지 못했어요. 잠시 후 다시 시도해 주세요.")}
          </p>
        )}

        <Button type="submit" size="lg" disabled={!password.ready || reset.isPending} className="mt-2">
          {reset.isPending ? <Spinner className="border-white/40 border-t-white" /> : "비밀번호 바꾸기"}
        </Button>
      </form>

      {/* 링크가 죽었을 때 갈 곳을 늘 보여 준다 — 실패하고 나서야 알려 주면 한 번 더 막힌다. */}
      <p className="mt-6 text-center text-[14px] text-grey-500">
        링크가 만료됐나요?{" "}
        <Link href="/forgot-password" className="font-semibold text-primary hover:underline">
          다시 받기
        </Link>
      </p>
    </div>
  );
}

function Failed({ children }: { children: React.ReactNode }) {
  return (
    <div>
      <h1 className="text-[26px] font-bold leading-snug text-grey-900">링크를 열 수 없어요</h1>
      <p role="alert" className="mt-3 text-[15px] leading-relaxed text-grey-500">
        {children}
      </p>
      <Link href="/forgot-password" className="mt-8 block">
        <Button size="lg" className="w-full">
          링크 다시 받기
        </Button>
      </Link>
    </div>
  );
}
