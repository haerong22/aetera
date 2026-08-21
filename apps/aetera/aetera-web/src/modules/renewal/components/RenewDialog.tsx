"use client";

import { useEffect, useState, type FormEvent } from "react";
import { Button } from "@/components/ui/Button";
import { Dialog } from "@/components/ui/Dialog";
import { Input } from "@/components/ui/Input";
import { useRenewNow, type Renewal } from "../api";
import { CYCLE_LABELS } from "../labels";

export function RenewDialog({
  open,
  onClose,
  renewal,
}: {
  open: boolean;
  onClose: () => void;
  renewal: Renewal | null;
}) {
  const renew = useRenewNow();
  const [nextExpiresAt, setNextExpiresAt] = useState("");
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (!open || !renewal) return;
    setFailed(false);
    setNextExpiresAt(renewal.nextExpiresAt ?? renewal.expiresAt);
  }, [open, renewal]);

  if (!renewal) return null;

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    setFailed(false);
    renew.mutate(
      { id: renewal!.id, nextExpiresAt },
      { onSuccess: () => onClose(), onError: () => setFailed(true) },
    );
  }

  return (
    <Dialog open={open} onClose={onClose} title="갱신했어요">
      <form className="flex flex-col gap-4" onSubmit={onSubmit}>
        <div className="rounded-xl bg-grey-50 px-4 py-3">
          <p className="text-[15px] font-semibold text-grey-900">{renewal.title}</p>
          <p className="mt-1 text-[13px] text-grey-500">
            지금 만기 {renewal.expiresAt} · 주기 {CYCLE_LABELS[renewal.cycle]}
          </p>
        </div>

        <Input
          label="다음 만기일"
          type="date"
          value={nextExpiresAt}
          required
          onChange={(event) => setNextExpiresAt(event.target.value)}
        />
        <p className="-mt-2 text-[13px] text-grey-500">
          주기에 맞춰 미리 채웠어요. 실제 갱신한 날짜가 다르면 바꿔 주세요.
        </p>

        {failed && (
          <p role="alert" className="text-[13px] text-danger">
            갱신하지 못했어요. 잠시 후 다시 시도해 주세요.
          </p>
        )}

        <div className="flex items-center gap-2">
          <Button variant="ghost" className="flex-1" disabled={renew.isPending} onClick={onClose}>
            취소
          </Button>
          <Button type="submit" className="flex-1" disabled={renew.isPending || !nextExpiresAt}>
            {renew.isPending ? "저장 중" : "갱신"}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
