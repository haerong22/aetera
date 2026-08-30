"use client";

import { inKoreanUnits } from "@/lib/money";
import { Input } from "./Input";

/** 사람이 실수로 0을 몇 개 더 붙여도 자바스크립트 정수 범위를 벗어나지 않는 자릿수. */
const MAX_DIGITS = 13;

/**
 * 원 단위 금액 입력.
 *
 * 화면에는 쉼표를 넣어 보여주고 **상태에는 숫자만 담는다** — 쉼표까지 상태에 들어가면
 * 부르는 쪽이 매번 벗겨 내야 하고, 한 곳이라도 빠뜨리면 `Number()` 가 NaN 을 낸다.
 *
 * 아래 한 줄은 높이를 고정해 둔다. 입력에 따라 나타났다 사라지면 그 아래 폼 전체가 들썩인다.
 */
export function MoneyInput({
  label,
  value,
  hint,
  onChange,
}: {
  label: string;
  /** 숫자만 담긴 문자열. 빈 문자열이면 아직 입력 전이다. */
  value: string;
  /** 금액이 없을 때 대신 보여줄 안내. */
  hint?: string;
  onChange: (digits: string) => void;
}) {
  const amount = Number(value || "0");
  const units = value === "" ? null : inKoreanUnits(amount);

  return (
    <div>
      <Input
        label={label}
        inputMode="numeric"
        placeholder="0"
        value={value === "" ? "" : amount.toLocaleString("ko-KR")}
        onChange={(event) => onChange(event.target.value.replace(/\D/g, "").slice(0, MAX_DIGITS))}
        className="text-right tabular-nums"
      />
      <p className="mt-1 h-4 text-[12px] text-grey-500">{units ?? hint ?? ""}</p>
    </div>
  );
}
