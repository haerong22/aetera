"use client";

import { useState } from "react";
import { Button } from "@/components/ui/Button";

/**
 * 기준일 입력. 시작 화면과 변경 화면이 같은 폼을 쓴다 —
 * 사용자에게는 둘 다 "퇴사일을 이걸로 한다"는 하나의 행동이다.
 *
 * 폼을 닫는 것은 부르는 쪽이 [onSubmit] 의 `onDone` 으로 정한다. 제출하자마자 닫으면
 * 저장에 실패해도 알릴 곳이 사라져서, 사용자가 바뀐 줄 알고 넘어간다.
 */
export function AnchorDateForm({
  anchorLabel,
  initialValue,
  submitLabel,
  pending,
  failed,
  onSubmit,
  onCancel,
}: {
  anchorLabel: string;
  initialValue: string;
  submitLabel: string;
  pending: boolean;
  failed: boolean;
  onSubmit: (anchorDate: string) => void;
  onCancel?: () => void;
}) {
  const [value, setValue] = useState(initialValue);
  // 한 번도 제출하지 않았으면 지난 실패를 보여주지 않는다 — 열자마자 빨간 글씨가 있으면
  // 방금 내가 뭘 잘못한 줄 안다.
  const [submitted, setSubmitted] = useState(false);

  return (
    <form
      className="flex flex-col gap-2"
      onSubmit={(event) => {
        event.preventDefault();
        if (!value) return;
        setSubmitted(true);
        onSubmit(value);
      }}
    >
      <div className="flex flex-wrap items-center gap-2">
        <input
          type="date"
          required
          value={value}
          aria-label={anchorLabel}
          onChange={(event) => setValue(event.target.value)}
          className="h-11 rounded-(--radius-input) border border-grey-200 bg-white px-3.5 text-[15px] text-grey-900 outline-none focus:border-primary"
        />
        <Button type="submit" disabled={pending || !value}>
          {pending ? "저장 중" : submitLabel}
        </Button>
        {onCancel && (
          <Button type="button" variant="ghost" onClick={onCancel} disabled={pending}>
            취소
          </Button>
        )}
      </div>
      {failed && submitted && (
        <p role="alert" className="text-[13px] text-danger">
          저장하지 못했어요. 잠시 후 다시 시도해 주세요.
        </p>
      )}
    </form>
  );
}
