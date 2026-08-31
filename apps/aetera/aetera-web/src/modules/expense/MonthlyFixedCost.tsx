"use client";

import type { MonthlyFixedCostProps } from "../types";
import { useExpenses } from "./api";

/**
 * 내 한 달 고정지출을 다른 모듈에 건네주는 통로.
 *
 * 퇴사 준비가 "버틸 개월 수"를 계산할 때 쓴다. 저쪽은 이 모듈을 import 하지 않는다 —
 * 코어의 능력 레지스트리가 켜진 모듈에서 이 컴포넌트를 찾아 자리에 끼워 준다.
 *
 * 합계를 여기서 다시 계산하지 않고 서버가 준 값을 그대로 넘긴다.
 * 연으로 합쳐 한 번만 나누는 규칙이 두 벌이 되면 화면마다 다른 숫자가 나온다.
 */
export function MonthlyFixedCost({ children }: MonthlyFixedCostProps) {
  const { data } = useExpenses();
  return <>{children(data?.monthlyTotal ?? null)}</>;
}
