"use client";

import { useRef, useState, type FormEvent } from "react";
import { Plus, Trash2, X } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Dialog } from "@/components/ui/Dialog";
import { Input } from "@/components/ui/Input";
import { MoneyInput } from "@/components/ui/MoneyInput";
import { Select, optionsFrom } from "@/components/ui/Select";
import { useDeleteSnapshot, useSaveSnapshot, type AssetCategory, type AssetEntry } from "../api";
import { CATEGORY_LABELS, formatMonth } from "../labels";

/** 화면에서 편집하는 동안의 한 줄. 금액을 문자열로 들고 있어야 입력 중 상태를 잃지 않는다. */
interface Draft {
  key: string;
  name: string;
  category: AssetCategory;
  amount: string;
}

/**
 * 이 다이얼로그는 **열릴 때 마운트된다**(부모가 조건부로 그린다).
 *
 * 그래서 초기 줄 목록을 `useState` 초기값으로 한 번만 깐다. `useEffect` 로 깔면 의존성에
 * `entries` 가 들어가는데, 그건 보드가 새로 올 때마다 참조가 바뀌는 배열이라
 * **열어 놓고 편집하는 도중에 입력이 통째로 초기화된다.**
 */
export function SnapshotDialog({
  month,
  entries,
  existing,
  onClose,
}: {
  /** 기록할 달(`"2026-09-01"`). */
  month: string;
  /** 지난 기록. 계좌 목록을 매달 다시 적지 않도록 그대로 깔아 준다. */
  entries: AssetEntry[];
  /** 이 달이 이미 기록돼 있는가. 지우기를 보여줄지 정한다. */
  existing: boolean;
  onClose: () => void;
}) {
  const save = useSaveSnapshot();
  const remove = useDeleteSnapshot();

  /**
   * 줄마다 붙는 키. 이름이나 순번으로 만들면 안 된다 —
   * 이름은 겹칠 수 있고, 순번은 지웠다 추가하면 앞서 쓴 값과 부딪힌다.
   * 부딪히면 한 줄을 고칠 때 다른 줄까지 함께 바뀐다.
   */
  const nextKey = useRef(0);
  function newKey(): string {
    nextKey.current += 1;
    return `draft-${nextKey.current}`;
  }

  function blank(): Draft {
    return { key: newKey(), name: "", category: "CASH", amount: "" };
  }

  const [drafts, setDrafts] = useState<Draft[]>(() =>
    entries.length > 0
      ? entries.map((entry) => ({
          key: newKey(),
          name: entry.name,
          category: entry.category,
          amount: String(entry.amount),
        }))
      : [blank()],
  );
  const [failed, setFailed] = useState(false);

  function patch(key: string, change: Partial<Draft>) {
    setDrafts((previous) => previous.map((draft) => (draft.key === key ? { ...draft, ...change } : draft)));
  }

  const busy = save.isPending || remove.isPending;
  const filled = drafts.filter((draft) => draft.name.trim().length > 0);
  const done = { onSuccess: onClose, onError: () => setFailed(true) };

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    setFailed(false);
    save.mutate(
      {
        month,
        // 이름이 빈 줄은 지운 것으로 본다 — 삭제 버튼을 못 찾아 이름만 지우는 사람이 있다.
        entries: filled.map((draft) => ({
          name: draft.name.trim(),
          category: draft.category,
          amount: Number(draft.amount || "0"),
        })),
      },
      done,
    );
  }

  return (
    <Dialog open onClose={onClose} title={`${formatMonth(month)} 자산 기록`}>
      <form onSubmit={onSubmit} className="flex flex-col gap-3">
        <p className="text-[13px] leading-relaxed text-grey-600">
          계좌·부동산·대출의 지금 잔액을 적어 주세요. 부채도 금액은 그대로 적으면 순자산에서 알아서
          빠집니다.
        </p>

        <ul className="flex flex-col gap-3">
          {drafts.map((draft) => (
            <li key={draft.key} className="rounded-(--radius-card) border border-grey-200 bg-white p-3">
              <div className="flex items-start gap-2">
                <div className="min-w-0 flex-1">
                  <Input
                    label="이름"
                    value={draft.name}
                    maxLength={100}
                    placeholder="주거래 통장, 전세보증금, 전세대출"
                    onChange={(event) => patch(draft.key, { name: event.target.value })}
                  />
                </div>
                <button
                  type="button"
                  aria-label={`${draft.name || "이 줄"} 지우기`}
                  onClick={() => setDrafts((previous) => previous.filter((it) => it.key !== draft.key))}
                  className="mt-7 flex size-8 shrink-0 items-center justify-center rounded-lg text-grey-400 transition-colors hover:bg-grey-100 hover:text-danger"
                >
                  <X size={16} aria-hidden />
                </button>
              </div>

              <div className="mt-2 grid gap-2 sm:grid-cols-2">
                <Select
                  label="분류"
                  value={draft.category}
                  options={optionsFrom(CATEGORY_LABELS)}
                  onChange={(event) => patch(draft.key, { category: event.target.value as AssetCategory })}
                />
                <MoneyInput
                  label="잔액"
                  value={draft.amount}
                  onChange={(digits) => patch(draft.key, { amount: digits })}
                />
              </div>
            </li>
          ))}
        </ul>

        <Button type="button" variant="secondary" onClick={() => setDrafts((previous) => [...previous, blank()])}>
          <Plus size={16} aria-hidden />
          줄 추가
        </Button>

        {failed && (
          <p role="alert" className="text-[13px] text-danger">
            저장하지 못했어요. 잠시 후 다시 시도해 주세요.
          </p>
        )}

        <div className="mt-2 flex items-center gap-2">
          <Button type="button" variant="ghost" className="flex-1" disabled={busy} onClick={onClose}>
            취소
          </Button>
          <Button type="submit" className="flex-1" disabled={busy || filled.length === 0}>
            {save.isPending ? "저장 중" : "저장"}
          </Button>
        </div>

        {existing && (
          <button
            type="button"
            disabled={busy}
            onClick={() => {
              setFailed(false);
              remove.mutate(month, done);
            }}
            className="mt-1 inline-flex items-center justify-center gap-1 text-[13px] font-medium text-grey-400 transition-colors hover:text-danger"
          >
            <Trash2 size={14} aria-hidden />
            {formatMonth(month)} 기록 지우기
          </button>
        )}
      </form>
    </Dialog>
  );
}
