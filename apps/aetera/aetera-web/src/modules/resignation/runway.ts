import { localToday } from "@/lib/date";

/** 한 달의 평균 길이. 개월 수를 날짜로 되돌릴 때만 쓴다. */
const DAYS_PER_MONTH = 30.44;

/**
 * 버틸 개월 수의 세 갈래. 화면이 셋을 다르게 그리므로 하나로 뭉개지 않는다.
 *
 * - `months` — 이만큼 버틴다
 * - `never` — 들어오는 돈이 나가는 돈을 넘어 바닥나는 달이 없다
 * - `unknown` — 셈이 성립하지 않는다(가진 돈이 없거나 나가는 돈이 없다)
 *
 * `never` 를 큰 숫자로 적지 않는 이유: "9999개월 버팀"은 거짓말이다.
 */
export type Runway =
  /** [netBurn] 은 실제로 통장이 줄어드는 속도 — 화면이 "한 달 얼마씩" 으로 그대로 쓴다. */
  | { kind: "months"; months: number; netBurn: number }
  | { kind: "never" }
  | { kind: "unknown" };

export function runwayMonths(cash: number, monthlyBurn: number, monthlyIncome: number): Runway {
  if (cash <= 0 || monthlyBurn <= 0) return { kind: "unknown" };

  const netBurn = monthlyBurn - monthlyIncome;
  return netBurn > 0 ? { kind: "months", months: cash / netBurn, netBurn } : { kind: "never" };
}

/**
 * 돈이 떨어지는 달.
 *
 * 개월 수를 **날짜로 환산해 오늘에 더한다.** 달 단위로 더하면서 1일로 옮기면
 * "오늘이 31일"이라는 사실과 소수부(0.8개월 ≈ 24일)가 함께 날아가 한 달이 앞당겨진다.
 * 일 단위 덧셈은 말일 넘침도 알아서 처리한다.
 *
 * 날짜까지는 말하지 않는다 — "대략 몇 달"에서 나온 값이라 하루 단위로 찍으면
 * 계산이 실제보다 정밀해 보인다.
 */
export function formatRunsOut(months: number): string {
  const date = localToday();
  date.setDate(date.getDate() + Math.round(months * DAYS_PER_MONTH));
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월`;
}
