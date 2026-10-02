"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";
import { toLocalDateIso, localToday } from "@/lib/date";

/**
 * 평소와 견준 결과. **문턱값은 서버가 안다.**
 *
 * 여기 두면 타임라인(서버가 글을 짓는다)과 목록이 갈려, 같은 달을 두고 한쪽은 "많음"
 * 다른 쪽은 "비슷"이라 할 수 있다. 화면이 정하는 것은 말과 색뿐이다.
 */
export type SpendingComparison = "MORE" | "LESS" | "SIMILAR";

export interface SpendingRecord {
  /** 그 달의 1일. 서버가 맞춰서 준다. */
  month: string;
  amount: number;
  note?: string;
  /** 평균보다 얼마나 많은가. 음수면 적게 쓴 달이다. 견줄 평균이 없으면 없다. */
  versusAverage?: number;
  comparison?: SpendingComparison;
}

export interface SpendingBoard {
  /** 최근 달부터. */
  records: SpendingRecord[];
  /**
   * 최근 몇 달의 평균. 기록이 없으면 없다.
   *
   * **적지 않은 달은 세지 않는다.** 0 으로 치면 평균이 주저앉아 런웨이가 실제보다 길게 나온다 —
   * 어느 달을 세고 어느 달을 뺄지는 서버가 한 곳에서 정한다.
   */
  monthlyAverage?: number;
  /** 평균을 낸 달 수. 한두 달치를 "평소"로 읽지 않도록 화면이 함께 보여 준다. */
  averagedMonths: number;
}

export interface SpendingInput {
  amount: number;
  note?: string;
}

const BASE = "/api/v1/modules/spending/records";
const RECORDS_KEY = ["spending", "records"] as const;

/**
 * `today` 를 함께 보낸다.
 *
 * 평균이 "최근 몇 달"을 보려면 오늘이 언제인지 알아야 하는데 **서버의 오늘은 사용자의
 * 오늘이 아니다** — 시간대가 다르면 달이 하나 어긋난다. 브라우저의 달력이 정답이다.
 */
function todayParam(): string {
  return `today=${toLocalDateIso(localToday())}`;
}

export function useSpending() {
  return useQuery({
    queryKey: RECORDS_KEY,
    queryFn: () => apiFetch<SpendingBoard>(`${BASE}?${todayParam()}`),
  });
}

/** 변경 API 가 화면 전체를 돌려주므로 다시 조회하지 않는다 — 평균까지 한 번에 맞는다. */
function useSpendingMutation<TVariables>(mutationFn: (variables: TVariables) => Promise<SpendingBoard>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: (board) => queryClient.setQueryData(RECORDS_KEY, board),
    onError: () => void queryClient.invalidateQueries({ queryKey: RECORDS_KEY }),
  });
}

/** 같은 달로 몇 번을 보내도 결과가 같다 — 서버가 덮어쓴다. */
export function useSaveSpending() {
  return useSpendingMutation(({ month, input }: { month: string; input: SpendingInput }) =>
    apiFetch<SpendingBoard>(`${BASE}/${month}?${todayParam()}`, {
      method: "PUT",
      body: JSON.stringify(input),
    }),
  );
}

export function useDeleteSpending() {
  return useSpendingMutation((month: string) =>
    apiFetch<SpendingBoard>(`${BASE}/${month}?${todayParam()}`, { method: "DELETE" }),
  );
}
