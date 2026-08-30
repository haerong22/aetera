"use client";

import { useEffect, useState, type FormEvent } from "react";
import { Trash2 } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Dialog } from "@/components/ui/Dialog";
import { Input } from "@/components/ui/Input";
import { MoneyInput } from "@/components/ui/MoneyInput";
import { Select, optionsFrom } from "@/components/ui/Select";
import {
  useCreateExpense,
  useDeleteExpense,
  useUpdateExpense,
  type Expense,
  type ExpenseCategory,
  type ExpenseCycle,
} from "../api";
import { CATEGORY_LABELS, CYCLE_LABELS } from "../labels";

export function ExpenseDialog({
  open,
  onClose,
  expense,
}: {
  open: boolean;
  onClose: () => void;
  expense?: Expense | null;
}) {
  const create = useCreateExpense();
  const update = useUpdateExpense();
  const remove = useDeleteExpense();

  const [title, setTitle] = useState("");
  const [category, setCategory] = useState<ExpenseCategory>("SUBSCRIPTION");
  const [amount, setAmount] = useState("");
  const [cycle, setCycle] = useState<ExpenseCycle>("MONTHLY");
  const [memo, setMemo] = useState("");
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (!open) return;
    setFailed(false);
    setTitle(expense?.title ?? "");
    setCategory(expense?.category ?? "SUBSCRIPTION");
    setAmount(expense ? String(expense.amount) : "");
    setCycle(expense?.cycle ?? "MONTHLY");
    setMemo(expense?.memo ?? "");
  }, [open, expense]);

  const busy = create.isPending || update.isPending || remove.isPending;
  const amountValue = Number(amount || "0");
  const ready = title.trim().length > 0 && amountValue > 0;

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (!ready) return;
    setFailed(false);

    const input = {
      title: title.trim(),
      category,
      amount: amountValue,
      cycle,
      memo: memo.trim() || undefined,
    };
    const done = { onSuccess: onClose, onError: () => setFailed(true) };

    if (expense) update.mutate({ id: expense.id, input }, done);
    else create.mutate(input, done);
  }

  return (
    <Dialog open={open} onClose={onClose} title={expense ? "고정지출 수정" : "고정지출 추가"}>
      <form onSubmit={onSubmit} className="flex flex-col gap-3">
        <Input
          label="이름"
          autoFocus
          value={title}
          maxLength={100}
          placeholder="월세, 넷플릭스, 실비보험"
          onChange={(event) => setTitle(event.target.value)}
        />

        <MoneyInput label="금액" value={amount} onChange={setAmount} />

        <Select
          label="주기"
          value={cycle}
          options={optionsFrom(CYCLE_LABELS)}
          onChange={(event) => setCycle(event.target.value as ExpenseCycle)}
        />

        <Select
          label="분류"
          value={category}
          options={optionsFrom(CATEGORY_LABELS)}
          onChange={(event) => setCategory(event.target.value as ExpenseCategory)}
        />

        <Input
          label="메모 (선택)"
          value={memo}
          maxLength={500}
          placeholder="계약 조건, 해지 방법 같은 걸 적어 두세요"
          onChange={(event) => setMemo(event.target.value)}
        />

        {failed && (
          <p role="alert" className="text-[13px] text-danger">
            저장하지 못했어요. 잠시 후 다시 시도해 주세요.
          </p>
        )}

        <div className="mt-2 flex items-center gap-2">
          <Button type="button" variant="ghost" className="flex-1" disabled={busy} onClick={onClose}>
            취소
          </Button>
          <Button type="submit" className="flex-1" disabled={busy || !ready}>
            {busy ? "저장 중" : "저장"}
          </Button>
        </div>

        {expense && (
          <button
            type="button"
            disabled={busy}
            onClick={() => remove.mutate(expense.id, { onSuccess: onClose, onError: () => setFailed(true) })}
            className="mt-1 inline-flex items-center justify-center gap-1 text-[13px] font-medium text-grey-400 transition-colors hover:text-danger"
          >
            <Trash2 size={14} aria-hidden />
            삭제
          </button>
        )}
      </form>
    </Dialog>
  );
}
