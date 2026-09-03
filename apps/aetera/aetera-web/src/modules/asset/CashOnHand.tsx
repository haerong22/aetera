"use client";

import type { AmountProviderProps } from "../types";
import { useAssets } from "./api";
import { formatMonth } from "./labels";

/**
 * 당장 쓸 수 있는 현금을 다른 모듈에 건네주는 통로.
 *
 * **언제 값인지 반드시 함께 말한다.** 자산은 달마다 찍는 기록이라 마지막이 석 달 전일 수 있는데,
 * 그 숫자가 "지금 가진 돈" 자리에 말없이 앉으면 버틸 개월 수가 조용히 틀린다.
 *
 * 투자·부동산은 빼고 현금만 센다(서버의 `cashTotal`) — 몇 달 버티나를 물을 때
 * 집을 팔아서 버틴다고 셈하지는 않는다.
 */
export function CashOnHand({ children }: AmountProviderProps) {
  const { data } = useAssets();
  return (
    <>
      {children(
        data
          ? {
              amount: data.cashTotal,
              note: data.latestMonth ? `${formatMonth(data.latestMonth)} 기준` : "아직 기록이 없어요",
            }
          : null,
      )}
    </>
  );
}
