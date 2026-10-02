"use client";

import { useState } from "react";
import { Plus, Receipt } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/StatusCard";
import { SummaryCard } from "@/components/ui/SummaryCard";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { won } from "@/lib/money";
import { ModuleDisabledNotice, isModuleDisabled } from "../ModuleDisabledNotice";
import { useSpending, type SpendingRecord } from "./api";
import { currentMonthIso, monthLabel, previousMonthIso } from "./labels";
import { SpendingDialog } from "./components/SpendingDialog";

/**
 * 평균과 견준 한 줄.
 *
 * **어느 갈래인지는 서버가 정한다**(`comparison`). 문턱값을 여기 두면 타임라인과 갈려서,
 * 같은 달을 두고 타임라인은 "많음"이라 하고 이 목록은 "비슷"이라 할 수 있다.
 * 여기서 정하는 것은 말과 색뿐이다.
 */
function versusAverage(record: SpendingRecord): { text: string; tone: string } | null {
  if (record.comparison === undefined || record.versusAverage === undefined) return null;

  switch (record.comparison) {
    case "MORE":
      return { text: `평소보다 ${won(record.versusAverage)} 많아요`, tone: "text-danger" };
    case "LESS":
      return { text: `평소보다 ${won(-record.versusAverage)} 적어요`, tone: "text-primary" };
    case "SIMILAR":
      return { text: "평소와 비슷해요", tone: "text-grey-500" };
  }
}

function SpendingRow({ record, onEdit }: { record: SpendingRecord; onEdit: () => void }) {
  const versus = versusAverage(record);

  return (
    <li className="flex flex-wrap items-center gap-x-3 gap-y-1 py-3.5">
      <div className="min-w-0 flex-1">
        <button
          type="button"
          onClick={onEdit}
          className="block max-w-full truncate text-left text-[15px] font-semibold text-grey-900 hover:underline"
        >
          {monthLabel(record.month)}
        </button>
        {record.note && (
          <p className="mt-0.5 truncate text-[12.5px] text-grey-500">{record.note}</p>
        )}
      </div>
      <div className="text-right">
        <p className="text-[15px] font-semibold text-grey-900">{won(record.amount)}</p>
        {versus && <p className={`mt-0.5 text-[12.5px] ${versus.tone}`}>{versus.text}</p>}
      </div>
    </li>
  );
}

/**
 * 변동지출.
 *
 * **달에 한 번, 숫자 하나.** 영수증을 매번 적게 하는 도구는 두 주면 안 쓴다 —
 * 카드 명세서 합계를 옮겨 적는 정도의 일로 끝내는 것이 이 모듈의 전제다.
 *
 * 기본으로 여는 달은 **지난 달**이다. 달이 끝나야 쓴 돈이 확정되고 명세서도 그때 나온다.
 */
export function SpendingPage() {
  const { data, isPending, isError, error, refetch } = useSpending();
  const [editing, setEditing] = useState<{ month: string; record: SpendingRecord | null } | null>(null);

  if (isModuleDisabled(error)) return <ModuleDisabledNotice title="변동지출" />;
  if (isPending) return <PageSpinner />;
  if (isError || !data) return <ErrorState onRetry={() => void refetch()} />;

  const records = data.records;
  const recorded = new Set(records.map((record) => record.month));
  const lastMonth = previousMonthIso();

  /** 아직 안 적은 달 중 먼저 권할 쪽. 지난 달을 이미 적었으면 이번 달로 넘어간다. */
  const suggested = recorded.has(lastMonth) ? currentMonthIso() : lastMonth;

  function open(month: string) {
    setEditing({ month, record: records.find((record) => record.month === month) ?? null });
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-grey-900">변동지출</h1>
          <p className="mt-1 text-[14px] text-grey-500">
            달에 한 번, 그 달에 쓴 돈 총액만 적어 두세요.
          </p>
        </div>
        {/*
          아직 적은 달이 없으면 여기 두지 않는다 — 빈 상태가 같은 단추를 이미 들고 있어
          화면에 똑같은 것이 둘이 된다. 그때는 빈 상태 쪽이 무엇을 왜 적는지까지 말해 준다.
        */}
        {records.length > 0 && (
          <Button onClick={() => open(suggested)}>
            <Plus size={16} aria-hidden />
            {monthLabel(suggested)} 적기
          </Button>
        )}
      </div>

      {data.monthlyAverage !== undefined && (
        <SummaryCard>
          <div>
            <p className="text-[13px] font-medium text-grey-600">평소 한 달에</p>
            <p className="mt-0.5 text-[28px] leading-tight font-bold text-primary tabular-nums">
              {won(data.monthlyAverage)}
            </p>
          </div>
          {/*
            * 몇 달치 평균인지 반드시 함께 적는다. 두 달치 평균을 "평소"로 읽으면
            * 퇴사 준비의 런웨이가 그만큼 흔들리는데, 보는 사람은 그 사실을 알 수 없다.
            */}
          <p className="text-[12.5px] text-grey-500">
            최근 {data.averagedMonths}개월 평균
            {data.averagedMonths < 3 && <span className="block">달이 쌓이면 더 정확해져요</span>}
          </p>
        </SummaryCard>
      )}

      {records.length === 0 ? (
        <EmptyState
          icon={<Receipt size={22} aria-hidden />}
          title="아직 적은 달이 없어요"
          description="지난 달 카드 명세서 합계를 한 번 옮겨 적어 보세요. 달이 쌓이면 평소 얼마 쓰는지 보여요."
          action={<Button onClick={() => open(lastMonth)}>{monthLabel(lastMonth)} 적기</Button>}
        />
      ) : (
        <Card className="p-0 sm:p-0">
          <ul className="divide-y divide-grey-100 px-5 sm:px-6">
            {records.map((record) => (
              <SpendingRow key={record.month} record={record} onEdit={() => open(record.month)} />
            ))}
          </ul>
        </Card>
      )}

      <SpendingDialog
        open={editing !== null}
        onClose={() => setEditing(null)}
        month={editing?.month ?? suggested}
        record={editing?.record}
      />
    </div>
  );
}
