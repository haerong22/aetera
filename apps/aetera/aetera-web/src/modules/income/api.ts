"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";

export type IncomeCategory =
  | "SALARY"
  | "SIDE"
  | "RENTAL"
  | "FINANCIAL"
  | "PENSION"
  | "BENEFIT"
  | "ETC";

export type IncomeCycle = "MONTHLY" | "QUARTERLY" | "HALF_YEARLY" | "YEARLY";

export interface Income {
  id: string;
  title: string;
  category: IncomeCategory;
  amount: number;
  cycle: IncomeCycle;
  /** 주기가 달라도 견줄 수 있게 서버가 환산해 준 값. */
  yearlyAmount: number;
  /** 일을 그만둬도 이어지는 돈인지. 분류에서 나오지만 화면이 규칙을 또 갖지 않도록 서버가 말해 준다. */
  continuesAfterLeaving: boolean;
  memo?: string;
}

/** 목록과 합계는 늘 함께 온다 — 항목이 바뀌면 합계도 함께 움직인다. */
export interface IncomeBoard {
  items: Income[];
  monthlyTotal: number;
  yearlyTotal: number;
  /** 월급처럼 그만두면 멈추는 것을 뺀 한 달 소득. */
  monthlyContinuing: number;
}

export interface IncomeInput {
  title: string;
  category: IncomeCategory;
  amount: number;
  cycle: IncomeCycle;
  memo?: string;
}

const BASE = "/api/v1/modules/income/items";
const INCOMES_KEY = ["income", "items"] as const;

export function useIncomes() {
  return useQuery({
    queryKey: INCOMES_KEY,
    queryFn: () => apiFetch<IncomeBoard>(BASE),
  });
}

/**
 * 변경 API 가 화면 전체를 돌려주므로 다시 조회하지 않는다 —
 * 응답을 그대로 캐시에 넣으면 합계까지 한 번에 맞는다.
 */
function useIncomeMutation<TVariables>(mutationFn: (variables: TVariables) => Promise<IncomeBoard>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: (board) => queryClient.setQueryData(INCOMES_KEY, board),
    onError: () => void queryClient.invalidateQueries({ queryKey: INCOMES_KEY }),
  });
}

export function useCreateIncome() {
  return useIncomeMutation((input: IncomeInput) =>
    apiFetch<IncomeBoard>(BASE, { method: "POST", body: JSON.stringify(input) }),
  );
}

export function useUpdateIncome() {
  return useIncomeMutation(({ id, input }: { id: string; input: IncomeInput }) =>
    apiFetch<IncomeBoard>(`${BASE}/${id}`, { method: "PUT", body: JSON.stringify(input) }),
  );
}

export function useDeleteIncome() {
  return useIncomeMutation((id: string) =>
    apiFetch<IncomeBoard>(`${BASE}/${id}`, { method: "DELETE" }),
  );
}
