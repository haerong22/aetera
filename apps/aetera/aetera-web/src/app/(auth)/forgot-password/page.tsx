"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Spinner } from "@/components/ui/Spinner";
import { errorMessage } from "@/lib/api-client";
import { useRequestPasswordReset } from "@/lib/passwordReset";

/**
 * 비밀번호 찾기 — 메일 보내기까지.
 *
 * **"보냈어요"라고 말하지 않는다.** 서버는 가입 여부를 구분해 답하지 않는데, 화면이
 * "보냈어요"라고 단정하면 그 구분을 화면이 대신 해 버리는 셈이다. 남의 주소를 넣어
 * 본 사람이 가입 여부를 알게 된다.
 *
 * 대신 **"가입돼 있다면 보냈어요"** 로 적는다. 정말 가입한 사람에게는 같은 뜻이고,
 * 떠보는 사람에게는 아무것도 알려 주지 않는다.
 */
export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const request = useRequestPasswordReset();

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    request.mutate(email);
  }

  if (request.isSuccess) {
    return (
      <div>
        <h1 className="text-[26px] font-bold leading-snug text-grey-900">메일을 확인해 주세요</h1>
        <p className="mt-3 text-[15px] leading-relaxed text-grey-500">
          <b className="font-semibold text-grey-700">{email}</b> 으로 가입돼 있다면 비밀번호를 다시
          정하는 링크를 보냈어요. 30분 동안만 열리고, 한 번 쓰면 닫혀요.
        </p>
        <p className="mt-2 text-[14px] text-grey-400">
          메일이 오지 않으면 스팸함도 확인해 주세요.
        </p>

        <Button
          type="button"
          variant="secondary"
          size="lg"
          className="mt-8 w-full"
          onClick={() => request.reset()}
        >
          다른 주소로 다시 보내기
        </Button>

        <p className="mt-6 text-center text-[14px] text-grey-500">
          <Link href="/login" className="font-semibold text-primary hover:underline">
            로그인으로 돌아가기
          </Link>
        </p>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-[26px] font-bold leading-snug text-grey-900">비밀번호를 잊으셨나요?</h1>
      <p className="mt-2 text-[15px] text-grey-500">
        가입한 이메일을 적어 주시면 다시 정하는 링크를 보내드려요.
      </p>

      <form onSubmit={onSubmit} className="mt-10 flex flex-col gap-4">
        <Input
          label="이메일"
          type="email"
          autoComplete="email"
          autoFocus
          placeholder="you@example.com"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          required
        />

        {request.isError && (
          <p
            role="alert"
            className="rounded-xl bg-danger-light px-4 py-3 text-[13px] font-medium text-danger"
          >
            {errorMessage(request.error, "보내지 못했어요. 잠시 후 다시 시도해 주세요.")}
          </p>
        )}

        <Button type="submit" size="lg" disabled={request.isPending} className="mt-2">
          {request.isPending ? <Spinner className="border-white/40 border-t-white" /> : "링크 받기"}
        </Button>
      </form>

      <p className="mt-6 text-center text-[14px] text-grey-500">
        생각났나요?{" "}
        <Link href="/login" className="font-semibold text-primary hover:underline">
          로그인
        </Link>
      </p>
    </div>
  );
}
