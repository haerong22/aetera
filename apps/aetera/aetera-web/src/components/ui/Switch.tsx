"use client";

import { useId } from "react";
import { cn } from "./cn";

/**
 * 켜고 끄는 스위치.
 *
 * 보이는 것은 만들어 낸 모양이지만 실제로는 체크박스다 — 키보드와 스크린 리더가
 * 브라우저 기본 동작을 그대로 쓰게 하려는 것이다. `div` 에 role 을 붙여 흉내 내면
 * 스페이스바·포커스·폼 제출을 전부 손으로 다시 만들어야 하고, 그중 하나는 반드시 빠뜨린다.
 */
export function Switch({
  checked,
  onChange,
  label,
  disabled = false,
}: {
  checked: boolean;
  onChange: (checked: boolean) => void;
  /** 스위치만 있으면 무엇을 켜는지 알 수 없다. 화면에는 숨기고 읽어 주기만 한다. */
  label: string;
  disabled?: boolean;
}) {
  const id = useId();

  return (
    <label
      htmlFor={id}
      className={cn(
        "relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors duration-200",
        checked ? "bg-primary" : "bg-grey-300",
        disabled ? "cursor-not-allowed opacity-50" : "cursor-pointer",
      )}
    >
      <input
        id={id}
        type="checkbox"
        role="switch"
        checked={checked}
        disabled={disabled}
        onChange={(event) => onChange(event.target.checked)}
        className="peer sr-only"
      />
      <span className="sr-only">{label}</span>
      <span
        aria-hidden
        className={cn(
          "pointer-events-none inline-block size-4 rounded-full bg-white shadow-sm transition-transform duration-200",
          checked ? "translate-x-6" : "translate-x-1",
        )}
      />
      <span
        aria-hidden
        className="pointer-events-none absolute inset-0 rounded-full peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-primary"
      />
    </label>
  );
}
