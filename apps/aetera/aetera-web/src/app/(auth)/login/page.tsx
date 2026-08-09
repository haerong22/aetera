"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { ApiError } from "@/lib/api-client";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Spinner } from "@/components/ui/Spinner";

export default function LoginPage() {
  const { login } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login({ email, password });
      router.replace("/dashboard");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "로그인에 실패했어요. 잠시 후 다시 시도해 주세요.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <h1 className="text-[26px] font-bold leading-snug text-grey-900">
        인생의 모든 순간,
        <br />
        <span className="text-primary">Aetera</span>에서 설계하세요
      </h1>
      <p className="mt-2 text-[15px] text-grey-500">필요한 모듈만 골라 쓰는 라이프 플랫폼</p>

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
          label="비밀번호"
          type="password"
          autoComplete="current-password"
          placeholder="8자 이상, 영문+숫자"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />
        {error && (
          <p className="rounded-xl bg-danger-light px-4 py-3 text-[13px] font-medium text-danger">{error}</p>
        )}
        <Button type="submit" size="lg" disabled={submitting} className="mt-2">
          {submitting ? <Spinner className="border-white/40 border-t-white" /> : "로그인"}
        </Button>
      </form>

      <p className="mt-6 text-center text-[14px] text-grey-500">
        아직 계정이 없나요?{" "}
        <Link href="/signup" className="font-semibold text-primary hover:underline">
          가입하기
        </Link>
      </p>
    </div>
  );
}
