"use client";

import type { AmountProviderProps } from "../types";
import { useIncomes } from "./api";

/**
 * 한 달에 들어오는 돈 **전부**를 다른 모듈에 건네주는 통로.
 *
 * [ContinuingIncome][./ContinuingIncome] 과 짝이지만 묻는 것이 다르다. 저쪽은 "그만두면
 * 얼마 남나"라서 월급을 빼고, 여기는 "지금 얼마 들어오나"라서 월급을 넣는다.
 * 능력을 둘로 나눈 이유가 그것이다 — 하나로 두면 받는 쪽이 무엇을 받았는지 알 수 없다.
 *
 * 언제 값인지는 밝히지 않는다. 등록해 둔 금액이라 조회 시점이 곧 지금이다.
 */
export function MonthlyIncome({ children }: AmountProviderProps) {
  const { data } = useIncomes();
  return <>{children(data ? { amount: data.monthlyTotal } : null)}</>;
}
