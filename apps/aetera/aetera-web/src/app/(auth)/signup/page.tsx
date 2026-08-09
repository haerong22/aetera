"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { ApiError } from "@/lib/api-client";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Spinner } from "@/components/ui/Spinner";

export default function SignUpPage() {
  const { signup } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await signup({ email, nickname, password });
      router.replace("/dashboard");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "가입에 실패했어요. 잠시 후 다시 시도해 주세요.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <h1 className="text-[26px] font-bold leading-snug text-grey-900">
        3초면 충분해요
      </h1>
      <p className="mt-2 text-[15px] text-grey-500">가입하면 바로 시작할 수 있어요.</p>

      <form onSubmit={onSubmit} className="mt-10 flex flex-col gap-4">
        <Input
          label="이메일"
          type="email"
          autoComplete="email"
          placeholder="you@example.com"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          required
        />
        <Input
          label="닉네임"
          autoComplete="nickname"
          placeholder="어떻게 불러 드릴까요?"
          maxLength={30}
          value={nickname}
          onChange={(event) => setNickname(event.target.value)}
          required
        />
        <Input
          label="비밀번호"
          type="password"
          autoComplete="new-password"
          placeholder="8자 이상, 영문+숫자"
          minLength={8}
          maxLength={64}
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />
        {error && (
          <p className="rounded-xl bg-danger-light px-4 py-3 text-[13px] font-medium text-danger">{error}</p>
        )}
        <Button type="submit" size="lg" disabled={submitting} className="mt-2">
          {submitting ? <Spinner className="border-white/40 border-t-white" /> : "가입하고 시작하기"}
        </Button>
      </form>

      <p className="mt-6 text-center text-[14px] text-grey-500">
        이미 계정이 있나요?{" "}
        <Link href="/login" className="font-semibold text-primary hover:underline">
          로그인
        </Link>
      </p>
    </div>
  );
}
