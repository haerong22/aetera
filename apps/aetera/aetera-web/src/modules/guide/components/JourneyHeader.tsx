"use client";

import { useState } from "react";
import { CalendarDays, PartyPopper, RotateCcw } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Dialog } from "@/components/ui/Dialog";
import { cn } from "@/components/ui/cn";
import type { GuideJourney, GuideProgress } from "../api";
import { formatDDay, formatKoreanDate } from "../dates";
import { AnchorDateForm } from "./AnchorDateForm";

/**
 * 시작한 뒤의 머리말: 남은 날짜, 기준일, 진행률.
 *
 * 진행률 막대는 **필수 항목 기준**이다. 참고용까지 넣으면 100% 에 닿을 수 없고,
 * 닿지 않는 막대는 아무도 보지 않게 된다.
 */
export function JourneyHeader({
  anchorLabel,
  journey,
  progress,
  onChangeAnchorDate,
  onReset,
  changePending,
  changeFailed,
  resetPending,
  resetFailed,
}: {
  anchorLabel: string;
  journey: GuideJourney;
  progress: GuideProgress;
  /** 저장에 성공하면 `onDone` 이 불린다 — 그때까지 폼을 열어 둬야 실패를 알릴 수 있다. */
  onChangeAnchorDate: (anchorDate: string, onDone: () => void) => void;
  onReset: () => void;
  changePending: boolean;
  changeFailed: boolean;
  resetPending: boolean;
  resetFailed: boolean;
}) {
  const [editing, setEditing] = useState(false);
  const [confirmingReset, setConfirmingReset] = useState(false);
  // 이번에 열어서 눌러 본 적이 있어야 실패를 보여준다. 지난 실패를 들고 열면
  // 아무것도 안 했는데 빨간 글씨가 떠 있다 ([AnchorDateForm] 의 `submitted` 와 같은 규칙).
  const [resetAttempted, setResetAttempted] = useState(false);

  function openResetDialog() {
    setResetAttempted(false);
    setConfirmingReset(true);
  }

  const completed = progress.requiredDone === progress.requiredTotal;
  const percent =
    progress.requiredTotal === 0 ? 0 : Math.round((progress.requiredDone / progress.requiredTotal) * 100);

  return (
    <Card className="flex flex-col gap-4">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <span
              className={cn(
                "rounded-(--radius-chip) px-2.5 py-1 text-[15px] font-bold tabular-nums",
                completed ? "bg-success-light text-success" : "bg-primary-light text-primary",
              )}
            >
              {formatDDay(journey.anchorDate)}
            </span>
            <span className="text-[15px] font-semibold text-grey-800">
              {formatKoreanDate(journey.anchorDate)}
            </span>
          </div>
          <p className="mt-1 flex items-center gap-1.5 text-[13px] text-grey-500">
            <CalendarDays size={14} aria-hidden />
            {anchorLabel}
          </p>
        </div>

        {!editing && (
          <div className="flex shrink-0 items-center gap-1">
            <Button size="sm" variant="secondary" onClick={() => setEditing(true)}>
              날짜 변경
            </Button>
            <Button size="sm" variant="ghost" onClick={openResetDialog}>
              <RotateCcw size={14} aria-hidden />
              초기화
            </Button>
          </div>
        )}
      </div>

      {editing && (
        <AnchorDateForm
          anchorLabel={anchorLabel}
          initialValue={journey.anchorDate}
          submitLabel="변경"
          pending={changePending}
          failed={changeFailed}
          onSubmit={(anchorDate) => onChangeAnchorDate(anchorDate, () => setEditing(false))}
          onCancel={() => setEditing(false)}
        />
      )}

      <div>
        <div className="mb-2 flex items-baseline justify-between gap-2">
          <span className="text-[14px] font-semibold text-grey-800">
            {completed ? (
              <span className="flex items-center gap-1.5 text-success">
                <PartyPopper size={15} aria-hidden />
                필수 항목을 모두 마쳤어요
              </span>
            ) : (
              `필수 ${progress.requiredDone}/${progress.requiredTotal} 완료`
            )}
          </span>
          <span className="text-[13px] text-grey-500 tabular-nums">
            전체 {progress.done}/{progress.total}
          </span>
        </div>
        <div
          role="progressbar"
          aria-valuenow={percent}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-label="필수 항목 진행률"
          className="h-2 w-full overflow-hidden rounded-full bg-grey-100"
        >
          <div
            className={cn(
              "h-full rounded-full transition-[width] duration-300",
              completed ? "bg-success" : "bg-primary",
            )}
            style={{ width: `${percent}%` }}
          />
        </div>
      </div>

      <Dialog open={confirmingReset} onClose={() => setConfirmingReset(false)} title="처음부터 다시 할까요?">
        <p className="text-[14.5px] leading-relaxed text-grey-600">
          {anchorLabel}과 지금까지 체크한 항목, 남겨 둔 메모가 모두 지워져요. 되돌릴 수 없어요.
        </p>
        {resetFailed && resetAttempted && (
          <p role="alert" className="mt-3 text-[13px] text-danger">
            초기화하지 못했어요. 잠시 후 다시 시도해 주세요.
          </p>
        )}
        <div className="mt-6 flex gap-2">
          <Button variant="ghost" className="flex-1" disabled={resetPending} onClick={() => setConfirmingReset(false)}>
            취소
          </Button>
          {/*
            성공하면 여정이 사라져 이 카드째로 사라지므로 다이얼로그를 따로 닫지 않는다.
            반대로 실패하면 열린 채로 남아 위의 안내를 보여준다 — 눌렀는데 아무 일도 안 일어나는 상태를 없앤다.
          */}
          <Button
            variant="danger"
            className="flex-1"
            disabled={resetPending}
            onClick={() => {
              setResetAttempted(true);
              onReset();
            }}
          >
            {resetPending ? "지우는 중" : "초기화"}
          </Button>
        </div>
      </Dialog>
    </Card>
  );
}
