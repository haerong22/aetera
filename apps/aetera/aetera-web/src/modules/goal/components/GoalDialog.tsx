"use client";

import { useEffect, useState, type FormEvent } from "react";
import { Trash2 } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Dialog } from "@/components/ui/Dialog";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { PERIOD_LABELS, useCreateGoal, useDeleteGoal, useUpdateGoal, type Goal, type GoalPeriod } from "../api";

export function GoalDialog({
  open,
  onClose,
  goal,
}: {
  open: boolean;
  onClose: () => void;
  goal?: Goal | null;
}) {
  const create = useCreateGoal();
  const update = useUpdateGoal();
  const remove = useDeleteGoal();

  const [title, setTitle] = useState("");
  const [period, setPeriod] = useState<GoalPeriod>("WEEKLY");
  const [target, setTarget] = useState(3);
  const [unit, setUnit] = useState("회");
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (!open) return;
    setFailed(false);
    setTitle(goal?.title ?? "");
    setPeriod(goal?.period ?? "WEEKLY");
    setTarget(goal?.target ?? 3);
    setUnit(goal?.unit ?? "회");
  }, [open, goal]);

  const busy = create.isPending || update.isPending || remove.isPending;
  const periodChanged = goal !== null && goal !== undefined && goal.period !== period;

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    setFailed(false);
    const input = { title, period, target, unit: unit.trim() || undefined };
    const options = { onSuccess: () => onClose(), onError: () => setFailed(true) };

    if (goal) update.mutate({ id: goal.id, input }, options);
    else create.mutate(input, options);
  }

  return (
    <Dialog open={open} onClose={onClose} title={goal ? "목표 수정" : "목표 추가"}>
      <form className="flex flex-col gap-4" onSubmit={onSubmit}>
        <Input
          label="무엇을 할까요"
          value={title}
          required
          maxLength={100}
          placeholder="운동하기"
          onChange={(event) => setTitle(event.target.value)}
        />

        <Select
          label="기간"
          value={period}
          options={Object.entries(PERIOD_LABELS).map(([value, label]) => ({ value, label }))}
          onChange={(event) => setPeriod(event.target.value as GoalPeriod)}
        />

        <div className="flex gap-3">
          <div className="flex-1">
            <Input
              label="목표치"
              type="number"
              min={1}
              max={100000}
              value={target}
              required
              onChange={(event) => setTarget(Number(event.target.value))}
            />
          </div>
          <div className="w-28">
            <Input
              label="단위"
              value={unit}
              maxLength={10}
              placeholder="회"
              onChange={(event) => setUnit(event.target.value)}
            />
          </div>
        </div>

        {periodChanged && (
          <p className="text-[13px] text-grey-500">기간을 바꾸면 지금까지 쌓인 진행은 0 부터 다시 시작해요.</p>
        )}

        {failed && (
          <p role="alert" className="text-[13px] text-danger">
            저장하지 못했어요. 잠시 후 다시 시도해 주세요.
          </p>
        )}

        <div className="flex items-center gap-2">
          {goal && (
            <Button
              variant="danger"
              disabled={busy}
              onClick={() => remove.mutate(goal.id, { onSuccess: onClose, onError: () => setFailed(true) })}
            >
              <Trash2 size={16} aria-hidden />
              삭제
            </Button>
          )}
          <Button type="submit" className="flex-1" disabled={busy || !title.trim() || target < 1}>
            {busy ? "저장 중" : "저장"}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
