"use client";

import { useState } from "react";
import { SummaryCard } from "@/components/ui/SummaryCard";
import { MoneyInput, ReadOnlyMoney } from "@/components/ui/MoneyInput";
import { localToday } from "@/lib/date";
import { won } from "@/lib/money";
import { WithAmount, useCashOnHand, useMonthlyFixedCost } from "../capabilityRegistry";
import { TaskToolPanel } from "../guide/components/TaskToolPanel";
import type { ProvidedAmount } from "../types";

/** 한 달의 평균 길이. 개월 수를 날짜로 되돌릴 때만 쓴다. */
const DAYS_PER_MONTH = 30.44;

/**
 * 돈이 떨어지는 달.
 *
 * 개월 수를 **날짜로 환산해 오늘에 더한다.** 달 단위로 더하면서 1일로 옮기면
 * "오늘이 31일"이라는 사실과 소수부(0.8개월 ≈ 24일)가 함께 날아가 한 달이 앞당겨진다.
 * 일 단위 덧셈은 말일 넘침도 알아서 처리한다.
 *
 * 날짜까지는 말하지 않는다 — "대략 몇 달"에서 나온 값이라 하루 단위로 찍으면
 * 계산이 실제보다 정밀해 보인다.
 */
function formatRunsOut(months: number): string {
  const date = localToday();
  date.setDate(date.getDate() + Math.round(months * DAYS_PER_MONTH));
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월`;
}

function Result({ cash, monthlyBurn }: { cash: number; monthlyBurn: number }) {
  // 나가는 돈이 없으면 답할 수 없다. 가진 돈이 0 이어도 마찬가지다.
  if (cash <= 0 || monthlyBurn <= 0) return null;
  const months = cash / monthlyBurn;

  return (
    <SummaryCard className="mt-4">
      <div>
        <p className="text-[13px] font-medium text-grey-600">지금 가진 돈으로</p>
        <p className="mt-0.5 text-[28px] leading-tight font-bold text-primary tabular-nums">
          약 {months.toFixed(1)}개월
        </p>
      </div>
      <div className="text-right">
        <p className="text-[13px] font-medium text-grey-600">{formatRunsOut(months)}쯤 바닥</p>
        <p className="mt-0.5 text-[13px] text-grey-600 tabular-nums">한 달 {won(monthlyBurn)}</p>
      </div>
    </SummaryCard>
  );
}

/**
 * 다른 모듈이 채워 준 금액. 출처와 시점을 아래에 적고, **직접 입력으로 빠져나갈 길을 둔다.**
 *
 * 그 길이 없으면 자산 모듈을 켜 두고 아직 기록하지 않은 사람은 0원에 갇혀 계산을 못 한다.
 * 가져온 값이 낡았다고 느끼는 사람도 마찬가지다.
 */
function LinkedAmount({
  label,
  provided,
  source,
  onOverride,
}: {
  label: string;
  provided: ProvidedAmount | null;
  source: string;
  onOverride: () => void;
}) {
  return (
    <ReadOnlyMoney
      label={label}
      value={provided === null ? "불러오는 중" : won(provided.amount)}
      footer={
        <>
          <span className="truncate">
            {source}
            {provided?.note ? ` · ${provided.note}` : ""}
          </span>
          <button
            type="button"
            onClick={onOverride}
            className="shrink-0 font-medium text-primary hover:underline"
          >
            직접 입력
          </button>
        </>
      }
    />
  );
}

/**
 * 다음 수입까지의 공백을 개월 수로 바꿔 본다.
 *
 * 자산과 고정지출 모듈을 켜 두었으면 두 금액이 자동으로 들어온다. 꺼져 있으면 직접 적는다 —
 * **꺼진 모듈 때문에 이 자리가 못 쓰게 되면 안 된다.**
 */
export function RunwayTool() {
  const CashOnHand = useCashOnHand();
  const MonthlyFixedCost = useMonthlyFixedCost();

  const [typedCash, setTypedCash] = useState("");
  const [typedFixedCost, setTypedFixedCost] = useState("");
  const [livingCost, setLivingCost] = useState("");

  /** 가져온 값을 물리고 직접 적기로 한 칸. 이 화면을 떠나면 잊는다. */
  const [overridden, setOverridden] = useState<{ cash: boolean; fixedCost: boolean }>({
    cash: false,
    fixedCost: false,
  });

  const linkCash = CashOnHand !== null && !overridden.cash;
  const linkFixedCost = MonthlyFixedCost !== null && !overridden.fixedCost;

  const missing = [CashOnHand ? null : "자산", MonthlyFixedCost ? null : "고정지출"].filter(
    (name): name is string => name !== null,
  );

  /** 직접 입력으로 넘어갈 때 가져온 값을 씨앗으로 깔아 준다 — 처음부터 다시 치게 하지 않는다. */
  function override(field: "cash" | "fixedCost", provided: ProvidedAmount | null) {
    const seed = provided === null ? "" : String(provided.amount);
    if (field === "cash") setTypedCash(seed);
    else setTypedFixedCost(seed);
    setOverridden((previous) => ({ ...previous, [field]: true }));
  }

  /**
   * 자리를 정하는 기준은 **값이 왔는가가 아니라 모듈이 켜졌는가**다.
   *
   * 도구는 펼칠 때 처음 마운트되므로 조회는 그제서야 나간다. 값으로 판단하면 응답이 오기 전
   * 한순간 직접 입력 칸이 떴다가 사라지고, 그 사이에 친 값은 조용히 버려진다.
   */
  function body(linkedCash: ProvidedAmount | null, linkedFixedCost: ProvidedAmount | null) {
    const cash = linkCash ? (linkedCash?.amount ?? null) : Number(typedCash || "0");
    const fixedCost = linkFixedCost ? (linkedFixedCost?.amount ?? null) : Number(typedFixedCost || "0");
    const monthlyBurn = fixedCost === null ? null : fixedCost + Number(livingCost || "0");

    return (
      <>
        <div className="mt-3 grid gap-3 sm:grid-cols-2">
          {linkCash ? (
            <LinkedAmount
              label="지금 가진 돈"
              provided={linkedCash}
              source="자산"
              onOverride={() => override("cash", linkedCash)}
            />
          ) : (
            <MoneyInput
              label="지금 가진 돈"
              value={typedCash}
              hint="퇴직금은 빼고, 당장 쓸 수 있는 돈만"
              onChange={setTypedCash}
            />
          )}

          {linkFixedCost ? (
            <LinkedAmount
              label="매달 나가는 고정지출"
              provided={linkedFixedCost}
              source="고정지출"
              onOverride={() => override("fixedCost", linkedFixedCost)}
            />
          ) : (
            <MoneyInput
              label="매달 나가는 고정지출"
              value={typedFixedCost}
              hint="월세·통신비·보험료"
              onChange={setTypedFixedCost}
            />
          )}

          <MoneyInput
            label="그 밖의 생활비 (선택)"
            value={livingCost}
            hint="식비·교통비처럼 매달 쓰는 돈"
            onChange={setLivingCost}
          />
        </div>

        {cash !== null && monthlyBurn !== null && <Result cash={cash} monthlyBurn={monthlyBurn} />}
      </>
    );
  }

  return (
    <TaskToolPanel
      title="몇 달이나 버틸 수 있는지 계산해 보기"
      description="다음 수입까지의 공백을 개월 수로 바꿔 보면, 퇴사일을 언제로 잡을지가 훨씬 선명해져요."
      footnote={
        <>
          실업급여, 퇴직금, 건강보험료 변동은 넣지 않은 숫자예요. 여유를 조금 더 잡아 두세요.
          {missing.length > 0 && ` ${missing.join("·")} 모듈을 켜면 금액이 자동으로 들어와요.`}
        </>
      }
    >
      <WithAmount provider={CashOnHand}>
        {(cash) => (
          <WithAmount provider={MonthlyFixedCost}>{(fixedCost) => body(cash, fixedCost)}</WithAmount>
        )}
      </WithAmount>
    </TaskToolPanel>
  );
}
