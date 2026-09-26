"use client";

import { useState, type FormEvent } from "react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Dialog } from "@/components/ui/Dialog";
import { Input } from "@/components/ui/Input";
import { apiFetch } from "@/lib/api-client";
import { useAuth } from "@/lib/auth";

/** 되돌릴 수 없는 일이라 손으로 적게 한다. 버튼 한 번으로 끝나면 안 된다. */
const CONFIRM_WORD = "탈퇴합니다";

/**
 * 탈퇴.
 *
 * **지우기 전에 내보내기를 권한다.** 순서를 바꿀 수 없는 일이라 — 지운 뒤에는
 * 가져갈 것이 없다 — 화면이 그 순서를 말해 준다.
 */
export function WithdrawSection() {
  const { logout } = useAuth();
  const [open, setOpen] = useState(false);
  const [typed, setTyped] = useState("");
  const [busy, setBusy] = useState(false);
  const [failed, setFailed] = useState(false);

  async function withdraw(event: FormEvent) {
    event.preventDefault();
    if (typed !== CONFIRM_WORD) return;

    setBusy(true);
    setFailed(false);
    try {
      await apiFetch<void>("/api/v1/me", { method: "DELETE" });
      /*
       * 앱 상태를 손님으로 되돌린다. 안에서 부르는 로그아웃 API 는 계정이 이미 없어 실패하지만,
       * `logout` 은 그 경우에도 로컬 세션을 끝내도록 되어 있다.
       */
      await logout();
    } catch {
      setFailed(true);
      setBusy(false);
    }
  }

  return (
    <section className="flex flex-col gap-3">
      <div>
        <h2 className="text-[17px] font-bold text-grey-900">탈퇴</h2>
        <p className="mt-1 text-[14px] text-grey-500">
          계정과 지금까지 적은 것을 모두 지워요. 되돌릴 수 없으니 가져갈 것이 있다면 먼저
          내려받아 주세요.
        </p>
      </div>

      <Card className="border-danger/20">
        <p className="text-[14px] text-grey-700">
          자산·소득·고정지출·목표·만기·일정·가이드 기록이 전부 사라지고, 같은 이메일로 다시
          가입할 수 있게 돼요.
        </p>
        <Button variant="ghost" className="mt-3 text-danger hover:bg-danger/5" onClick={() => setOpen(true)}>
          탈퇴하기
        </Button>
      </Card>

      <Dialog open={open} onClose={() => setOpen(false)} title="정말 탈퇴할까요?">
        <form onSubmit={withdraw} className="flex flex-col gap-3">
          <p className="text-[14px] leading-relaxed text-grey-700">
            지운 뒤에는 되돌릴 수 없어요. 계속하려면 아래에{" "}
            <b className="font-semibold text-grey-900">{CONFIRM_WORD}</b>라고 적어 주세요.
          </p>

          <Input
            label="확인"
            autoFocus
            value={typed}
            placeholder={CONFIRM_WORD}
            onChange={(event) => setTyped(event.target.value)}
          />

          {failed && (
            <p role="alert" className="text-[13px] text-danger">
              탈퇴하지 못했어요. 잠시 후 다시 시도해 주세요.
            </p>
          )}

          <div className="mt-2 flex items-center gap-2">
            <Button type="button" variant="ghost" className="flex-1" disabled={busy} onClick={() => setOpen(false)}>
              취소
            </Button>
            <Button
              type="submit"
              variant="danger"
              className="flex-1"
              disabled={busy || typed !== CONFIRM_WORD}
            >
              {busy ? "지우는 중" : "탈퇴"}
            </Button>
          </div>
        </form>
      </Dialog>
    </section>
  );
}
