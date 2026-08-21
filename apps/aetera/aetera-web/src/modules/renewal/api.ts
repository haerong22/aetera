"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";

export type RenewalCategory =
  | "INSURANCE"
  | "CONTRACT"
  | "CERTIFICATE"
  | "SUBSCRIPTION"
  | "HEALTH"
  | "ETC";

export type RenewalCycle =
  | "NONE"
  | "MONTHLY"
  | "QUARTERLY"
  | "HALF_YEARLY"
  | "YEARLY"
  | "TWO_YEARS"
  | "FIVE_YEARS"
  | "TEN_YEARS";

export interface Renewal {
  id: string;
  title: string;
  category: RenewalCategory;
  expiresAt: string;
  cycle: RenewalCycle;
  noticeDays: number;
  memo?: string;
  nextExpiresAt?: string;
  createdAt: string;
}

interface RenewalInput {
  title: string;
  category: RenewalCategory;
  expiresAt: string;
  cycle: RenewalCycle;
  noticeDays: number;
  memo?: string;
}

const BASE = "/api/v1/modules/renewal/items";
const RENEWALS_KEY = ["renewal", "items"] as const;

export function useRenewals() {
  return useQuery({
    queryKey: RENEWALS_KEY,
    queryFn: () => apiFetch<Renewal[]>(BASE),
  });
}

function useRenewalMutation<TVariables>(mutationFn: (variables: TVariables) => Promise<unknown>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: RENEWALS_KEY }),
  });
}

export function useCreateRenewal() {
  return useRenewalMutation((input: RenewalInput) =>
    apiFetch<Renewal>(BASE, { method: "POST", body: JSON.stringify(input) }),
  );
}

export function useUpdateRenewal() {
  return useRenewalMutation(({ id, input }: { id: string; input: RenewalInput }) =>
    apiFetch<Renewal>(`${BASE}/${id}`, { method: "PUT", body: JSON.stringify(input) }),
  );
}

export function useRenewNow() {
  return useRenewalMutation(({ id, nextExpiresAt }: { id: string; nextExpiresAt: string }) =>
    apiFetch<Renewal>(`${BASE}/${id}/renewals`, {
      method: "POST",
      body: JSON.stringify({ nextExpiresAt }),
    }),
  );
}

export function useDeleteRenewal() {
  return useRenewalMutation((id: string) => apiFetch<void>(`${BASE}/${id}`, { method: "DELETE" }));
}
