"use client";

import { useEffect, useState, type FormEvent } from "react";
import { Trash2 } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Dialog } from "@/components/ui/Dialog";
import { Input } from "@/components/ui/Input";
import { Select, optionsFrom } from "@/components/ui/Select";
import { isoFromToday } from "@/lib/date";
import {
  useCreateRenewal,
  useDeleteRenewal,
  useUpdateRenewal,
  type Renewal,
  type RenewalCategory,
  type RenewalCycle,
} from "../api";
import { CATEGORY_LABELS, CYCLE_LABELS } from "../labels";

export function RenewalDialog({
  open,
  onClose,
  renewal,
}: {
  open: boolean;
  onClose: () => void;
  renewal?: Renewal | null;
}) {
  const create = useCreateRenewal();
  const update = useUpdateRenewal();
  const remove = useDeleteRenewal();

  const [title, setTitle] = useState("");
  const [category, setCategory] = useState<RenewalCategory>("INSURANCE");
  const [expiresAt, setExpiresAt] = useState("");
  const [cycle, setCycle] = useState<RenewalCycle>("YEARLY");
  const [noticeDays, setNoticeDays] = useState(30);
  const [memo, setMemo] = useState("");
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (!open) return;
    setFailed(false);
    setTitle(renewal?.title ?? "");
    setCategory(renewal?.category ?? "INSURANCE");
    setExpiresAt(renewal?.expiresAt ?? isoFromToday(365));
    setCycle(renewal?.cycle ?? "YEARLY");
    setNoticeDays(renewal?.noticeDays ?? 30);
    setMemo(renewal?.memo ?? "");
  }, [open, renewal]);

  const busy = create.isPending || update.isPending || remove.isPending;

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    setFailed(false);
    const input = {
      title,
      category,
      expiresAt,
      cycle,
      noticeDays,
      memo: memo.trim() || undefined,
    };
    const options = { onSuccess: () => onClose(), onError: () => setFailed(true) };

    if (renewal) update.mutate({ id: renewal.id, input }, options);
    else create.mutate(input, options);
  }

  return (
    <Dialog open={open} onClose={onClose} title={renewal ? "만기 항목 수정" : "만기 항목 추가"}>
      <form className="flex flex-col gap-4" onSubmit={onSubmit}>
        <Input
          label="이름"
          value={title}
          required
          maxLength={100}
          placeholder="자동차보험"
          onChange={(event) => setTitle(event.target.value)}
        />

        <Select
          label="분류"
          value={category}
          options={optionsFrom(CATEGORY_LABELS)}
          onChange={(event) => setCategory(event.target.value as RenewalCategory)}
        />

        <Input
          label="만기일"
          type="date"
          value={expiresAt}
          required
          onChange={(event) => setExpiresAt(event.target.value)}
        />

        <Select
          label="갱신 주기"
          value={cycle}
          options={optionsFrom(CYCLE_LABELS)}
          onChange={(event) => setCycle(event.target.value as RenewalCycle)}
        />

        <Input
          label="며칠 전부터 알려드릴까요"
          type="number"
          min={0}
          max={365}
          value={noticeDays}
          onChange={(event) => setNoticeDays(Number(event.target.value))}
        />

        <div className="flex flex-col gap-1.5">
          <label htmlFor="renewal-memo" className="text-[13px] font-medium text-grey-600">
            메모
          </label>
          <textarea
            id="renewal-memo"
            value={memo}
            maxLength={500}
            placeholder="증권번호, 담당자 연락처처럼 갱신할 때 필요한 것"
            onChange={(event) => setMemo(event.target.value)}
            className="min-h-[72px] rounded-(--radius-input) border border-grey-200 bg-white p-3 text-[14px] text-grey-900 outline-none placeholder:text-grey-400 focus:border-primary"
          />
        </div>

        {failed && (
          <p role="alert" className="text-[13px] text-danger">
            저장하지 못했어요. 잠시 후 다시 시도해 주세요.
          </p>
        )}

        <div className="flex items-center gap-2">
          {renewal && (
            <Button
              variant="danger"
              size="md"
              disabled={busy}
              onClick={() => remove.mutate(renewal.id, { onSuccess: onClose, onError: () => setFailed(true) })}
            >
              <Trash2 size={16} aria-hidden />
              삭제
            </Button>
          )}
          <Button type="submit" className="flex-1" disabled={busy || !title.trim() || !expiresAt}>
            {busy ? "저장 중" : "저장"}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
