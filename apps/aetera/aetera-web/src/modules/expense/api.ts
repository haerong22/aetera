"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";

export type ExpenseCategory =
  | "HOUSING"
  | "UTILITY"
  | "COMMUNICATION"
  | "INSURANCE"
  | "LOAN"
  | "SUBSCRIPTION"
  | "TRANSPORT"
  | "EDUCATION"
  | "ETC";

export type ExpenseCycle = "MONTHLY" | "QUARTERLY" | "HALF_YEARLY" | "YEARLY";

export interface Expense {
  id: string;
  title: string;
  category: ExpenseCategory;
  amount: number;
  cycle: ExpenseCycle;
  /** 주기가 달라도 견줄 수 있게 서버가 환산해 준 값. */
  yearlyAmount: number;
  memo?: string;
  createdAt: string;
}

/** 목록과 합계는 늘 함께 온다 — 항목이 바뀌면 합계도 함께 움직인다. */
export interface ExpenseBoard {
  items: Expense[];
  monthlyTotal: number;
  yearlyTotal: number;
}

export interface ExpenseInput {
  title: string;
  category: ExpenseCategory;
  amount: number;
  cycle: ExpenseCycle;
  memo?: string;
}

const BASE = "/api/v1/modules/expense/items";
const EXPENSES_KEY = ["expense", "items"] as const;

export function useExpenses() {
  return useQuery({
    queryKey: EXPENSES_KEY,
    queryFn: () => apiFetch<ExpenseBoard>(BASE),
  });
}

/**
 * 변경 API 가 화면 전체를 돌려주므로 다시 조회하지 않는다 —
 * 응답을 그대로 캐시에 넣으면 합계까지 한 번에 맞는다.
 */
function useExpenseMutation<TVariables>(mutationFn: (variables: TVariables) => Promise<ExpenseBoard>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: (board) => queryClient.setQueryData(EXPENSES_KEY, board),
    onError: () => void queryClient.invalidateQueries({ queryKey: EXPENSES_KEY }),
  });
}

export function useCreateExpense() {
  return useExpenseMutation((input: ExpenseInput) =>
    apiFetch<ExpenseBoard>(BASE, { method: "POST", body: JSON.stringify(input) }),
  );
}

export function useUpdateExpense() {
  return useExpenseMutation(({ id, input }: { id: string; input: ExpenseInput }) =>
    apiFetch<ExpenseBoard>(`${BASE}/${id}`, { method: "PUT", body: JSON.stringify(input) }),
  );
}

export function useDeleteExpense() {
  return useExpenseMutation((id: string) =>
    apiFetch<ExpenseBoard>(`${BASE}/${id}`, { method: "DELETE" }),
  );
}
