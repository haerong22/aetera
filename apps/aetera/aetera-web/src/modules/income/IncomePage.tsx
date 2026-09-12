"use client";

import { useState } from "react";
import { Banknote, Plus } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/StatusCard";
import { SummaryCard } from "@/components/ui/SummaryCard";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { won } from "@/lib/money";
import { ModuleDisabledNotice, isModuleDisabled } from "../ModuleDisabledNotice";
import { useIncomes, type Income } from "./api";
import { CATEGORY_LABELS, CYCLE_LABELS } from "./labels";
import { IncomeDialog } from "./components/IncomeDialog";

function IncomeRow({
  income,
  monthlyTotal,
  onEdit,
}: {
  income: Income;
  monthlyTotal: number;
  onEdit: () => void;
}) {
  /*
   * 이 항목이 한 달 소득에서 차지하는 몫. 한 군데에 얼마나 기대고 있는지가 금액보다 먼저 눈에 든다.
   *
   * 여기 표시되는 월 환산은 반올림이라 손으로 더하면 합계와 몇 원 어긋난다. 합계는 원 단위까지
   * 맞아야 하므로 서버가 연으로 합친 뒤 한 번만 나눠서 따로 내려준다.
   */
  const monthly = Math.round(income.yearlyAmount / 12);
  const share = monthlyTotal > 0 ? Math.round((monthly / monthlyTotal) * 100) : 0;

  return (
    <li className="flex flex-wrap items-center gap-x-3 gap-y-1.5 py-3.5">
      <div className="min-w-0 flex-1">
        <button
          type="button"
          onClick={onEdit}
          className="block max-w-full truncate text-left text-[15px] font-semibold text-grey-900 hover:underline"
        >
          {income.title}
        </button>
        <p className="mt-0.5 truncate text-[12.5px] text-grey-500">
          {CATEGORY_LABELS[income.category]} · {CYCLE_LABELS[income.cycle]}
          {income.continuesAfterLeaving ? "" : " · 그만두면 멈춤"}
          {income.memo ? ` · ${income.memo}` : ""}
        </p>
      </div>

      <div className="shrink-0 text-right">
        <p className="text-[15px] font-semibold text-grey-900 tabular-nums">{won(income.amount)}</p>
        <p className="mt-0.5 text-[12px] text-grey-400 tabular-nums">
          {income.cycle === "MONTHLY" ? `한 달 소득의 ${share}%` : `월 ${won(monthly)} · ${share}%`}
        </p>
      </div>
    </li>
  );
}

export function IncomePage() {
  const { data: board, error, refetch } = useIncomes();
  const [editing, setEditing] = useState<Income | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);

  if (error && isModuleDisabled(error)) return <ModuleDisabledNotice title="소득" />;
  if (!board) return error ? <ErrorState onRetry={() => void refetch()} /> : <PageSpinner />;

  function openNew() {
    setEditing(null);
    setDialogOpen(true);
  }

  function openEdit(income: Income) {
    setEditing(income);
    setDialogOpen(true);
  }

  /**
   * 멈추는 항목이 하나라도 있는지. 전부 이어지는 소득이면 굳이 말하지 않는다 —
   * 뺀 것이 없으면 알림이 아니라 잡음이다.
   *
   * 두 합계의 차로 묻지 않는다. 둘 다 연으로 합친 뒤 나눈 값이라 그 차가 "멈추는 것의 월 환산"과
   * 꼭 같지는 않고, 아주 작은 항목은 차가 0이 되어 **있는데 없다고** 답한다.
   */
  const hasStopping = board.items.some((income) => !income.continuesAfterLeaving);

  return (
    <div className="flex max-w-3xl flex-col gap-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-grey-900">소득</h1>
          <p className="mt-1 text-[15px] text-grey-500">
            통장에 실제로 들어오는 금액으로 적어 주세요. 세전으로 적으면 계산이 헐거워져요.
          </p>
        </div>
        <Button onClick={openNew}>
          <Plus size={16} aria-hidden />
          추가
        </Button>
      </div>

      <SummaryCard>
        <div>
          <p className="text-[13px] font-medium text-grey-600">한 달에</p>
          <p className="mt-0.5 text-[28px] leading-tight font-bold text-primary tabular-nums">
            {won(board.monthlyTotal)}
          </p>
        </div>
        <div className="text-right">
          <p className="text-[13px] font-medium text-grey-600">1년이면</p>
          <p className="mt-0.5 text-[17px] font-bold text-grey-800 tabular-nums">{won(board.yearlyTotal)}</p>
        </div>
      </SummaryCard>

      {hasStopping && (
        <Card className="py-4">
          <p className="text-[14px] text-grey-700">
            일을 그만두면 이 중{" "}
            <b className="font-semibold text-grey-900">{won(board.monthlyContinuing)}</b>만 남아요.
          </p>
          <p className="mt-1 text-[12.5px] text-grey-500">
            월급은 멈추는 돈으로 봐요. 퇴사 준비의 &ldquo;몇 달 버티나&rdquo;가 이 금액을 씁니다.
          </p>
        </Card>
      )}

      {board.items.length === 0 ? (
        <EmptyState
          icon={<Banknote size={22} aria-hidden />}
          title="아직 등록한 소득이 없어요"
          description="월급부터 넣어 보세요. 부업, 월세 수입, 배당, 연금처럼 주기적으로 들어오는 것이면 됩니다."
          action={
            <Button variant="secondary" onClick={openNew}>
              첫 항목 추가하기
            </Button>
          }
        />
      ) : (
        <Card className="p-0 sm:p-0">
          <ul className="divide-y divide-grey-100 px-5 sm:px-6">
            {board.items.map((income) => (
              <IncomeRow
                key={income.id}
                income={income}
                monthlyTotal={board.monthlyTotal}
                onEdit={() => openEdit(income)}
              />
            ))}
          </ul>
        </Card>
      )}

      <IncomeDialog open={dialogOpen} onClose={() => setDialogOpen(false)} income={editing} />
    </div>
  );
}
