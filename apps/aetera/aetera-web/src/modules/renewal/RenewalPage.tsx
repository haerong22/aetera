"use client";

import { useState } from "react";
import { CalendarPlus, Plus, RotateCw, ShieldCheck } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { cn } from "@/components/ui/cn";
import { ModuleDisabledNotice, isModuleDisabled } from "../ModuleDisabledNotice";
import { useAddEventDialog } from "../capabilityRegistry";
import type { CalendarDraft } from "../types";
import { useRenewals, type Renewal } from "./api";
import { CATEGORY_LABELS, CYCLE_LABELS, formatExpiry, renewalStatus } from "./labels";
import { RenewalDialog } from "./components/RenewalDialog";
import { RenewDialog } from "./components/RenewDialog";

const STATUS_CLASS = {
  expired: "bg-danger-light text-danger",
  due: "bg-accent-light text-accent",
  fine: "bg-grey-100 text-grey-600",
} as const;

function RenewalRow({
  renewal,
  onEdit,
  onRenew,
  onAddToCalendar,
}: {
  renewal: Renewal;
  onEdit: () => void;
  onRenew: () => void;
  onAddToCalendar?: () => void;
}) {
  const status = renewalStatus(renewal);

  return (
    <li className="flex flex-wrap items-center gap-x-3 gap-y-2 py-3.5">
      <span className={cn("shrink-0 rounded-(--radius-chip) px-2 py-0.5 text-[12px] font-semibold", STATUS_CLASS[status])}>
        {formatExpiry(renewal)}
      </span>

      <div className="min-w-0 flex-1">
        <button type="button" onClick={onEdit} className="block max-w-full truncate text-left text-[15px] font-semibold text-grey-900 hover:underline">
          {renewal.title}
        </button>
        <p className="mt-0.5 truncate text-[12.5px] text-grey-500">
          {CATEGORY_LABELS[renewal.category]} · {renewal.expiresAt} · {CYCLE_LABELS[renewal.cycle]}
          {renewal.memo ? ` · ${renewal.memo}` : ""}
        </p>
      </div>

      <div className="flex shrink-0 items-center gap-1.5">
        {onAddToCalendar && (
          <Button size="sm" variant="ghost" onClick={onAddToCalendar}>
            <CalendarPlus size={14} aria-hidden />
            캘린더
          </Button>
        )}
        {renewal.cycle !== "NONE" && (
          <Button size="sm" variant="secondary" onClick={onRenew}>
            <RotateCw size={14} aria-hidden />
            갱신
          </Button>
        )}
      </div>
    </li>
  );
}

export function RenewalPage() {
  const { data: renewals, error, refetch } = useRenewals();
  const AddEventDialog = useAddEventDialog();

  const [editing, setEditing] = useState<Renewal | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [calendarDraft, setCalendarDraft] = useState<CalendarDraft | null>(null);
  const [renewing, setRenewing] = useState<Renewal | null>(null);

  if (error && isModuleDisabled(error)) return <ModuleDisabledNotice title="만기 관리" />;
  if (!renewals) return error ? <ErrorState onRetry={() => void refetch()} /> : <PageSpinner />;

  const needsAttention = renewals.filter((item) => renewalStatus(item) !== "fine");

  return (
    <div className="flex max-w-3xl flex-col gap-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-grey-900">만기 관리</h1>
          <p className="mt-1 text-[15px] text-grey-500">
            {renewals.length === 0
              ? "보험·계약·증명서의 만기를 모아 두면 갱신할 때를 놓치지 않아요."
              : needsAttention.length > 0
                ? `${needsAttention.length}건이 곧 만기이거나 이미 지났어요.`
                : "지금 챙길 만기는 없어요."}
          </p>
        </div>
        <Button
          onClick={() => {
            setEditing(null);
            setDialogOpen(true);
          }}
        >
          <Plus size={17} aria-hidden /> 항목 추가
        </Button>
      </div>

      {renewals.length === 0 ? (
        <Card className="flex flex-col items-center gap-3 py-14 text-center">
          <span className="flex size-14 items-center justify-center rounded-3xl bg-primary-light text-primary">
            <ShieldCheck size={26} aria-hidden />
          </span>
          <div>
            <p className="text-lg font-bold text-grey-900">아직 등록한 만기가 없어요</p>
            <p className="mt-1 text-[14px] text-grey-500">
              자동차보험, 실손보험, 전세계약, 여권처럼 놓치면 곤란한 것부터 넣어 보세요.
            </p>
          </div>
        </Card>
      ) : (
        <Card className="p-0 sm:p-0">
          <ul className="divide-y divide-grey-100 px-5 sm:px-6">
            {renewals.map((renewal) => (
              <RenewalRow
                key={renewal.id}
                renewal={renewal}
                onEdit={() => {
                  setEditing(renewal);
                  setDialogOpen(true);
                }}
                onRenew={() => setRenewing(renewal)}
                onAddToCalendar={
                  AddEventDialog
                    ? () =>
                        setCalendarDraft({
                          title: `[만기] ${renewal.title}`,
                          description: renewal.memo,
                          date: renewal.expiresAt,
                        })
                    : undefined
                }
              />
            ))}
          </ul>
        </Card>
      )}

      <RenewalDialog open={dialogOpen} onClose={() => setDialogOpen(false)} renewal={editing} />

      <RenewDialog open={renewing !== null} onClose={() => setRenewing(null)} renewal={renewing} />

      {AddEventDialog && calendarDraft && (
        <AddEventDialog open onClose={() => setCalendarDraft(null)} draft={calendarDraft} />
      )}
    </div>
  );
}
