"use client";

import type { AmountProviderProps } from "../types";
import { useIncomes } from "./api";

/**
 * 일을 그만둬도 이어지는 한 달 소득을 다른 모듈에 건네주는 통로.
 *
 * 퇴사 준비가 "버틸 개월 수"를 계산할 때 쓴다. 저쪽은 이 모듈을 import 하지 않는다 —
 * 코어의 능력 레지스트리가 켜진 모듈에서 이 컴포넌트를 찾아 자리에 끼워 준다.
 *
 * **전체 소득이 아니라 `monthlyContinuing` 을 건넨다.** 월급까지 넘기면 퇴사하면 끊길 돈으로
 * 버티는 셈이 되어, 답이 실제보다 한참 넉넉해지거나 아예 "바닥나지 않는다"고 말하게 된다.
 * 무엇이 멈추는지는 소득 모듈이 아는 일이라 그 판단을 여기서 끝내고 숫자만 넘긴다.
 *
 * 뺀 것이 있을 때만 그렇다고 밝힌다 — 월급이 없는 사람에게 "월급 제외"는 잡음이다.
 */
export function ContinuingIncome({ children }: AmountProviderProps) {
  const { data } = useIncomes();
  if (!data) return <>{children(null)}</>;

  // 두 합계의 차가 아니라 항목을 본다 — 차는 연으로 합쳐 나눈 값끼리라 아주 작은 항목에서 0이 된다.
  const excluded = data.items.some((income) => !income.continuesAfterLeaving);
  return (
    <>
      {children({
        amount: data.monthlyContinuing,
        note: excluded ? "그만두면 멈추는 돈은 뺀 금액" : undefined,
      })}
    </>
  );
}
