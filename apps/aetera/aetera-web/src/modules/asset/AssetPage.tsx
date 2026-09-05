"use client";

import { useState } from "react";
import { PiggyBank, TrendingDown, TrendingUp } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/StatusCard";
import { SummaryCard } from "@/components/ui/SummaryCard";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { cn } from "@/components/ui/cn";
import { localToday, toLocalDateIso } from "@/lib/date";
import { won } from "@/lib/money";
import { ModuleDisabledNotice, isModuleDisabled } from "../ModuleDisabledNotice";
import { useAssets, type AssetBoard } from "./api";
import { CATEGORY_LABELS, formatMonth } from "./labels";
import { SnapshotDialog } from "./components/SnapshotDialog";

/** 이번 달의 1일. 기록의 단위가 달이므로 며칠인지는 쓰지 않는다. */
function thisMonth(): string {
  const date = localToday();
  date.setDate(1);
  return toLocalDateIso(date);
}

function Change({ amount }: { amount: number }) {
  if (amount === 0) return <span className="text-grey-500">지난 기록과 같아요</span>;

  const up = amount > 0;
  const Icon = up ? TrendingUp : TrendingDown;
  return (
    <span className={cn("inline-flex items-center gap-1 font-semibold", up ? "text-success" : "text-danger")}>
      <Icon size={14} aria-hidden />
      {up ? "+" : "−"}
      {won(Math.abs(amount))}
    </span>
  );
}

/**
 * 순자산 추이. 축도 눈금도 없이 막대만 세운다 —
 * 여기서 읽어야 하는 건 정확한 값이 아니라 **어느 쪽으로 가고 있는가**다.
 */
function History({ history }: { history: AssetBoard["history"] }) {
  if (history.length < 2) return null;

  // 음수 순자산도 그려야 하므로 바닥을 0 이 아니라 최솟값에 맞춘다.
  const values = history.map((point) => point.netWorth);
  const floor = Math.min(0, ...values);
  const ceiling = Math.max(0, ...values);
  const span = ceiling - floor || 1;

  return (
    <Card>
      <p className="text-[14px] font-bold text-grey-900">순자산 추이</p>
      <ul className="mt-4 flex items-end gap-1.5" style={{ height: 96 }}>
        {history.map((point) => (
          <li key={point.month} className="flex min-w-0 flex-1 flex-col justify-end">
            <span
              aria-hidden
              className={cn("w-full rounded-t", point.netWorth < 0 ? "bg-danger/50" : "bg-primary/60")}
              style={{ height: `${Math.max(2, ((point.netWorth - floor) / span) * 100)}%` }}
            />
            <span className="sr-only">
              {formatMonth(point.month)} {won(point.netWorth)}
            </span>
          </li>
        ))}
      </ul>
      <div className="mt-1.5 flex justify-between text-[11px] text-grey-400">
        <span>{formatMonth(history[0].month)}</span>
        <span>{formatMonth(history[history.length - 1].month)}</span>
      </div>
    </Card>
  );
}

export function AssetPage() {
  const { data: board, error, refetch } = useAssets();
  const [dialogOpen, setDialogOpen] = useState(false);

  if (error && isModuleDisabled(error)) return <ModuleDisabledNotice title="자산" />;
  if (!board) return error ? <ErrorState onRetry={() => void refetch()} /> : <PageSpinner />;

  const month = thisMonth();
  // "이번 달을 이미 적었는가"는 브라우저가 자기 달력으로 판단한다. 서버는 사용자의 오늘을 모른다.
  const recordedThisMonth = board.latestMonth === month;

  return (
    <div className="flex max-w-3xl flex-col gap-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-grey-900">자산</h1>
          <p className="mt-1 text-[15px] text-grey-500">
            한 달에 한 번 잔액만 적어 두면, 순자산이 어느 쪽으로 가고 있는지 보여요.
          </p>
        </div>
        <Button onClick={() => setDialogOpen(true)}>
          {recordedThisMonth ? "이번 달 수정하기" : "이번 달 기록하기"}
        </Button>
      </div>

      {board.latestMonth === undefined ? (
        <EmptyState
          icon={<PiggyBank size={22} aria-hidden />}
          title="아직 기록이 없어요"
          description="통장 잔액, 전세보증금, 대출 잔액을 한 번 적어 두면 다음 달부터는 금액만 고치면 돼요."
          action={
            <Button variant="secondary" onClick={() => setDialogOpen(true)}>
              첫 기록 남기기
            </Button>
          }
        />
      ) : (
        <>
          <SummaryCard>
            <div>
              <p className="text-[13px] font-medium text-grey-600">순자산</p>
              <p
                className={cn(
                  "mt-0.5 text-[28px] leading-tight font-bold tabular-nums",
                  board.netWorth < 0 ? "text-danger" : "text-primary",
                )}
              >
                {won(board.netWorth)}
              </p>
            </div>
            <div className="text-right text-[13px]">
              <p className="font-medium text-grey-600">{formatMonth(board.latestMonth)} 기준</p>
              <p className="mt-0.5 tabular-nums">
                {board.changeFromPrevious === undefined ? (
                  <span className="text-grey-500">첫 기록이에요</span>
                ) : (
                  <Change amount={board.changeFromPrevious} />
                )}
              </p>
            </div>
          </SummaryCard>

          {!recordedThisMonth && (
            <p className="rounded-(--radius-card) bg-warning-light px-4 py-3 text-[13.5px] leading-relaxed text-grey-700">
              마지막 기록이 {formatMonth(board.latestMonth)}이에요. 이번 달 잔액을 적으면 추이가 이어집니다.
            </p>
          )}

          <Card className="p-0 sm:p-0">
            <ul className="divide-y divide-grey-100 px-5 sm:px-6">
              {/* 이름은 겹칠 수 있어 키로 못 쓴다. 이 목록은 저장할 때마다 통째로 갈리므로 순번이 안전하다. */}
              {board.entries.map((entry, index) => (
                <li key={index} className="flex items-center justify-between gap-3 py-3.5">
                  <div className="min-w-0">
                    <p className="truncate text-[15px] font-semibold text-grey-900">{entry.name}</p>
                    <p className="mt-0.5 text-[12.5px] text-grey-500">{CATEGORY_LABELS[entry.category]}</p>
                  </div>
                  <p
                    className={cn(
                      "shrink-0 text-[15px] font-semibold tabular-nums",
                      entry.signedAmount < 0 ? "text-danger" : "text-grey-900",
                    )}
                  >
                    {entry.signedAmount < 0 ? "−" : ""}
                    {won(entry.amount)}
                  </p>
                </li>
              ))}
            </ul>
          </Card>

          <History history={board.history} />
        </>
      )}

      {/* 열릴 때 마운트한다 — 그래야 다이얼로그가 지난 기록을 한 번만 깔고, 편집 중에 초기화되지 않는다. */}
      {dialogOpen && (
        <SnapshotDialog
          month={month}
          entries={board.entries}
          existing={recordedThisMonth}
          onClose={() => setDialogOpen(false)}
        />
      )}
    </div>
  );
}
