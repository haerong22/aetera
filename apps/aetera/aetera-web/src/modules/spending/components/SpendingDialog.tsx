"use client";

import { useEffect, useState, type FormEvent } from "react";
import { Trash2 } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Dialog } from "@/components/ui/Dialog";
import { Input } from "@/components/ui/Input";
import { MoneyInput } from "@/components/ui/MoneyInput";
import { useDeleteSpending, useSaveSpending, type SpendingRecord } from "../api";
import { monthLabel } from "../labels";

/**
 * 한 달의 변동지출을 적는다.
 *
 * 달은 **고를 수 없다** — 열 때 정해져서 들어온다. 목록에서 그 달을 눌러 열거나
 * "이번 달 적기"로 열기 때문에, 안에서 또 고르게 하면 같은 선택을 두 번 하는 셈이다.
 *
 * 분류 칸이 없다. 이 모듈의 전제가 **달에 한 번, 숫자 하나**라서다 — 식비·교통비를
 * 나누려면 영수증 단위로 적어야 하고, 그러면 두 주 뒤에 안 쓴다.
 */
export function SpendingDialog({
  open,
  onClose,
  month,
  record,
}: {
  open: boolean;
  onClose: () => void;
  /** 적을 달. `YYYY-MM-01`. */
  month: string;
  /** 이미 적어 둔 것. 있으면 고치는 중이다. */
  record?: SpendingRecord | null;
}) {
  const save = useSaveSpending();
  const remove = useDeleteSpending();

  const [amount, setAmount] = useState("");
  const [note, setNote] = useState("");
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (!open) return;
    setFailed(false);
    setAmount(record ? String(record.amount) : "");
    setNote(record?.note ?? "");
  }, [open, record]);

  const busy = save.isPending || remove.isPending;

  /*
   * 0원도 보낼 수 있다. 한 푼도 안 쓴 달은 드물지만, 적어 둘 수 있어야 **안 적은 달과
   * 구분**된다 — 안 적은 달은 평균에서 빠지고 0원이라 적은 달은 평균을 끌어내린다.
   * 그래서 "금액이 0보다 큰가"가 아니라 "뭐라도 적었는가"로 본다.
   */
  const ready = amount !== "";

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (!ready) return;
    setFailed(false);

    save.mutate(
      { month, input: { amount: Number(amount), note: note.trim() || undefined } },
      { onSuccess: onClose, onError: () => setFailed(true) },
    );
  }

  function onDelete() {
    setFailed(false);
    remove.mutate(month, { onSuccess: onClose, onError: () => setFailed(true) });
  }

  return (
    <Dialog open={open} onClose={onClose} title={`${monthLabel(month)} 쓴 돈`}>
      <form onSubmit={onSubmit} className="flex flex-col gap-4">
        <MoneyInput
          label="그 달에 쓴 돈"
          value={amount}
          hint="카드 명세서 합계를 그대로 옮겨 적으면 돼요"
          onChange={setAmount}
        />

        <Input
          label="메모 (선택)"
          value={note}
          placeholder="추석 선물, 병원비처럼 그 달에 특별했던 것"
          maxLength={200}
          onChange={(event) => setNote(event.target.value)}
        />

        {failed && (
          <p role="alert" className="text-[13px] text-danger">
            저장하지 못했어요. 잠시 후 다시 시도해 주세요.
          </p>
        )}

        <div className="mt-2 flex items-center gap-2">
          {/* 고치는 중일 때만 보인다 — 아직 없는 달에 지우기를 두면 무엇을 지우는지 알 수 없다. */}
          {record && (
            <Button
              type="button"
              variant="ghost"
              className="text-danger hover:bg-danger/5"
              disabled={busy}
              onClick={onDelete}
              aria-label="이 달 기록 지우기"
            >
              <Trash2 size={16} aria-hidden />
            </Button>
          )}
          <Button type="button" variant="ghost" className="flex-1" disabled={busy} onClick={onClose}>
            취소
          </Button>
          <Button type="submit" className="flex-1" disabled={!ready || busy}>
            {busy ? "저장 중" : "저장"}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
