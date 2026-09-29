import { Suspense } from "react";
import { PageSpinner } from "@/components/ui/Spinner";
import { ResetPasswordForm } from "./ResetPasswordForm";

/**
 * `useSearchParams` 는 Suspense 경계 안에서만 쓸 수 있다 — 없으면 이 페이지가
 * 통째로 클라이언트 렌더로 밀려나고, `next build` 가 그 사실을 경고로 알려 준다.
 * 그래서 폼을 따로 두고 여기서 감싼다.
 */
export default function ResetPasswordPage() {
  return (
    <Suspense fallback={<PageSpinner />}>
      <ResetPasswordForm />
    </Suspense>
  );
}
