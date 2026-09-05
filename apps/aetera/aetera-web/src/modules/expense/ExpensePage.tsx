"use client";

import { useState } from "react";
import { Plus, Wallet } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/StatusCard";
import { SummaryCard } from "@/components/ui/SummaryCard";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { won } from "@/lib/money";
import { ModuleDisabledNotice, isModuleDisabled } from "../ModuleDisabledNotice";
import { useExpenses, type Expense } from "./api";
import { CATEGORY_LABELS, CYCLE_LABELS } from "./labels";
import { ExpenseDialog } from "./components/ExpenseDialog";

function ExpenseRow({
  expense,
  monthlyTotal,
  onEdit,
}: {
  expense: Expense;
  monthlyTotal: number;
  onEdit: () => void;
}) {
  /*
   * 이 항목이 한 달 부담에서 차지하는 몫. 무엇부터 줄일지 정하는 화면이라 금액보다 비중이 먼저 눈에 든다.
   * 주기가 다른 항목도 같은 잣대로 봐야 견줄 수 있으므로, 월 결제가 아니어도 비중을 보여준다.
   *
   * 월 결제가 아니면 월 환산 금액을 함께 적는다 — 연 72만원이 왜 6% 인지 그 줄에서 답이 나와야 한다.
   * 여기 표시되는 월 환산은 반올림이라 손으로 더하면 합계와 몇 원 어긋난다. 합계는 원 단위까지
   * 맞아야 하므로 서버가 연으로 합친 뒤 한 번만 나눠서 따로 내려준다.
   */
  const monthly = Math.round(expense.yearlyAmount / 12);
  const share = monthlyTotal > 0 ? Math.round((monthly / monthlyTotal) * 100) : 0;

  return (
    <li className="flex flex-wrap items-center gap-x-3 gap-y-1.5 py-3.5">
      <div className="min-w-0 flex-1">
        <button
          type="button"
          onClick={onEdit}
          className="block max-w-full truncate text-left text-[15px] font-semibold text-grey-900 hover:underline"
        >
          {expense.title}
        </button>
        <p className="mt-0.5 truncate text-[12.5px] text-grey-500">
          {CATEGORY_LABELS[expense.category]} · {CYCLE_LABELS[expense.cycle]}
          {expense.memo ? ` · ${expense.memo}` : ""}
        </p>
      </div>

      <div className="shrink-0 text-right">
        <p className="text-[15px] font-semibold text-grey-900 tabular-nums">{won(expense.amount)}</p>
        <p className="mt-0.5 text-[12px] text-grey-400 tabular-nums">
          {expense.cycle === "MONTHLY" ? `한 달 부담의 ${share}%` : `월 ${won(monthly)} · ${share}%`}
        </p>
      </div>
    </li>
  );
}

export function ExpensePage() {
  const { data: board, error, refetch } = useExpenses();
  const [editing, setEditing] = useState<Expense | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);

  if (error && isModuleDisabled(error)) return <ModuleDisabledNotice title="고정지출" />;
  if (!board) return error ? <ErrorState onRetry={() => void refetch()} /> : <PageSpinner />;

  function openNew() {
    setEditing(null);
    setDialogOpen(true);
  }

  function openEdit(expense: Expense) {
    setEditing(expense);
    setDialogOpen(true);
  }

  return (
    <div className="flex max-w-3xl flex-col gap-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-grey-900">고정지출</h1>
          <p className="mt-1 text-[15px] text-grey-500">
            매달 자동으로 빠져나가는 돈을 모아 두면, 줄일 곳이 보여요.
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

      {board.items.length === 0 ? (
        <EmptyState
          icon={<Wallet size={22} aria-hidden />}
          title="아직 등록한 고정지출이 없어요"
          description="월세, 통신비, 보험료, 구독료처럼 매달 같은 날 빠져나가는 것부터 넣어 보세요."
          action={
            <Button variant="secondary" onClick={openNew}>
              첫 항목 추가하기
            </Button>
          }
        />
      ) : (
        <Card className="p-0 sm:p-0">
          <ul className="divide-y divide-grey-100 px-5 sm:px-6">
            {board.items.map((expense) => (
              <ExpenseRow
                key={expense.id}
                expense={expense}
                monthlyTotal={board.monthlyTotal}
                onEdit={() => openEdit(expense)}
              />
            ))}
          </ul>
        </Card>
      )}

      <ExpenseDialog open={dialogOpen} onClose={() => setDialogOpen(false)} expense={editing} />
    </div>
  );
}
