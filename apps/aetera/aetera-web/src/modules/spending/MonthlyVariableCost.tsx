"use client";

import type { AmountProviderProps } from "../types";
import { useSpending } from "./api";

/**
 * 한 달에 쓰는 변동지출을 다른 모듈에 건네주는 통로.
 *
 * 고정지출과 짝이지만 성격이 다르다. 저쪽은 **약속된 돈**이라 등록된 금액이 곧 답이고,
 * 여기는 **지나 봐야 아는 돈**이라 지난 달들의 평균이 답이다.
 *
 * 그래서 언제 값인지를 꼭 밝힌다 — "최근 6개월 평균"인지 "두 달치 평균"인지에 따라
 * 받는 쪽이 이 숫자를 얼마나 믿을지가 달라진다. 한 달치를 "평소"로 읽으면 안 된다.
 */
export function MonthlyVariableCost({ children }: AmountProviderProps) {
  const { data } = useSpending();

  if (!data || data.monthlyAverage === undefined) return <>{children(null)}</>;

  return (
    <>
      {children({
        amount: data.monthlyAverage,
        note: `최근 ${data.averagedMonths}개월 평균`,
      })}
    </>
  );
}
