"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";

export type AssetCategory = "CASH" | "INVESTMENT" | "REAL_ESTATE" | "PENSION" | "DEBT" | "ETC";

/** 보낼 때의 한 줄. 부호는 서버가 정하므로 금액만 보낸다. */
export interface AssetEntryInput {
  name: string;
  category: AssetCategory;
  amount: number;
}

/** 받을 때의 한 줄. */
export interface AssetEntry extends AssetEntryInput {
  /** 순자산에 더해질 부호 있는 금액. 부채면 음수 — 어느 분류가 빼는 쪽인지는 서버가 정한다. */
  signedAmount: number;
}

export interface AssetPoint {
  month: string;
  netWorth: number;
}

export interface AssetBoard {
  /** 없으면 아직 한 번도 기록하지 않은 상태 — 오류가 아니라 시작 전이다. */
  latestMonth?: string;
  entries: AssetEntry[];
  netWorth: number;
  cashTotal: number;
  changeFromPrevious?: number;
  /** 오래된 것부터. */
  history: AssetPoint[];
}

const BASE = "/api/v1/modules/asset/snapshots";
const ASSETS_KEY = ["asset", "snapshots"] as const;

export function useAssets() {
  return useQuery({
    queryKey: ASSETS_KEY,
    queryFn: () => apiFetch<AssetBoard>(BASE),
  });
}

/** 변경 API 가 화면 전체를 돌려주므로 다시 조회하지 않는다. */
function useAssetMutation<TVariables>(mutationFn: (variables: TVariables) => Promise<AssetBoard>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: (board) => queryClient.setQueryData(ASSETS_KEY, board),
    onError: () => void queryClient.invalidateQueries({ queryKey: ASSETS_KEY }),
  });
}

export function useSaveSnapshot() {
  return useAssetMutation(({ month, entries }: { month: string; entries: AssetEntryInput[] }) =>
    apiFetch<AssetBoard>(`${BASE}/${month}`, { method: "PUT", body: JSON.stringify({ entries }) }),
  );
}

export function useDeleteSnapshot() {
  return useAssetMutation((month: string) =>
    apiFetch<AssetBoard>(`${BASE}/${month}`, { method: "DELETE" }),
  );
}
