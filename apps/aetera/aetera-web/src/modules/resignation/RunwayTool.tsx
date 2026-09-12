"use client";

import { useState } from "react";
import { SummaryCard } from "@/components/ui/SummaryCard";
import { MoneyInput, ReadOnlyMoney } from "@/components/ui/MoneyInput";
import { localToday } from "@/lib/date";
import { won } from "@/lib/money";
import {
  WithAmounts,
  useCashOnHand,
  useContinuingIncome,
  useMonthlyFixedCost,
} from "../capabilityRegistry";
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

function Result({
  cash,
  monthlyBurn,
  monthlyIncome,
}: {
  cash: number;
  monthlyBurn: number;
  monthlyIncome: number;
}) {
  // 나가는 돈이 없으면 답할 수 없다. 가진 돈이 0 이어도 마찬가지다.
  if (cash <= 0 || monthlyBurn <= 0) return null;

  /** 실제로 통장이 줄어드는 속도. 들어오는 돈이 그만큼 나가는 돈을 메운다. */
  const netBurn = monthlyBurn - monthlyIncome;

  /*
   * 들어오는 돈이 나가는 돈을 넘으면 바닥나는 달이 없다.
   * 큰 숫자로 적으면 "9999개월 버팀" 같은 거짓말이 되므로 개월 수를 아예 말하지 않는다.
   */
  if (netBurn <= 0) {
    return (
      <SummaryCard className="mt-4">
        <div>
          <p className="text-[13px] font-medium text-grey-600">이어지는 소득만으로</p>
          <p className="mt-0.5 text-[22px] leading-tight font-bold text-primary">줄지 않아요</p>
        </div>
        <div className="text-right">
          <p className="text-[13px] text-grey-600 tabular-nums">
            한 달 {won(monthlyBurn)} 나가고
          </p>
          <p className="mt-0.5 text-[13px] text-grey-600 tabular-nums">{won(monthlyIncome)} 들어와요</p>
        </div>
      </SummaryCard>
    );
  }

  const months = cash / netBurn;

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
        <p className="mt-0.5 text-[13px] text-grey-600 tabular-nums">
          한 달 {won(netBurn)}씩 줄어요
        </p>
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

type Field = "cash" | "fixedCost" | "income";

/**
 * 다음 수입까지의 공백을 개월 수로 바꿔 본다.
 *
 * 자산·고정지출·소득 모듈을 켜 두었으면 세 금액이 자동으로 들어온다. 꺼져 있으면 직접 적는다 —
 * **꺼진 모듈 때문에 이 자리가 못 쓰게 되면 안 된다.**
 *
 * 소득에서 받아 오는 것은 전체가 아니라 **그만둬도 이어지는 돈**이다. 월급까지 세면
 * 퇴사하면 끊길 돈으로 버티는 셈이 되어 답이 한참 넉넉해진다. 무엇이 멈추는지는
 * 소득 모듈이 판단해서 넘겨 준다.
 */
export function RunwayTool() {
  const CashOnHand = useCashOnHand();
  const MonthlyFixedCost = useMonthlyFixedCost();
  const ContinuingIncome = useContinuingIncome();

  const [typedCash, setTypedCash] = useState("");
  const [typedFixedCost, setTypedFixedCost] = useState("");
  const [typedIncome, setTypedIncome] = useState("");
  const [livingCost, setLivingCost] = useState("");

  /** 가져온 값을 물리고 직접 적기로 한 칸. 이 화면을 떠나면 잊는다. */
  const [overridden, setOverridden] = useState<Record<Field, boolean>>({
    cash: false,
    fixedCost: false,
    income: false,
  });

  const linkCash = CashOnHand !== null && !overridden.cash;
  const linkFixedCost = MonthlyFixedCost !== null && !overridden.fixedCost;
  const linkIncome = ContinuingIncome !== null && !overridden.income;

  const missing = [
    CashOnHand ? null : "자산",
    MonthlyFixedCost ? null : "고정지출",
    ContinuingIncome ? null : "소득",
  ].filter((name): name is string => name !== null);

  const setTyped: Record<Field, (value: string) => void> = {
    cash: setTypedCash,
    fixedCost: setTypedFixedCost,
    income: setTypedIncome,
  };

  /** 직접 입력으로 넘어갈 때 가져온 값을 씨앗으로 깔아 준다 — 처음부터 다시 치게 하지 않는다. */
  function override(field: Field, provided: ProvidedAmount | null) {
    setTyped[field](provided === null ? "" : String(provided.amount));
    setOverridden((previous) => ({ ...previous, [field]: true }));
  }

  /**
   * 자리를 정하는 기준은 **값이 왔는가가 아니라 모듈이 켜졌는가**다.
   *
   * 도구는 펼칠 때 처음 마운트되므로 조회는 그제서야 나간다. 값으로 판단하면 응답이 오기 전
   * 한순간 직접 입력 칸이 떴다가 사라지고, 그 사이에 친 값은 조용히 버려진다.
   */
  function body(
    linkedCash: ProvidedAmount | null,
    linkedFixedCost: ProvidedAmount | null,
    linkedIncome: ProvidedAmount | null,
  ) {
    const cash = linkCash ? (linkedCash?.amount ?? null) : Number(typedCash || "0");
    const fixedCost = linkFixedCost ? (linkedFixedCost?.amount ?? null) : Number(typedFixedCost || "0");
    const monthlyBurn = fixedCost === null ? null : fixedCost + Number(livingCost || "0");

    /*
     * 소득은 아직 못 읽었으면 0 으로 본다. 나머지 둘과 달리 **없어도 답이 나오는 값**이라,
     * null 로 두고 기다리면 소득만 늦게 도착할 때 이미 맞던 계산이 사라졌다 다시 뜬다.
     * 0 으로 시작하면 답이 넉넉해지는 쪽이 아니라 빡빡해지는 쪽으로 틀리므로 안전하다.
     */
    const income = linkIncome ? (linkedIncome?.amount ?? 0) : Number(typedIncome || "0");

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

        {/*
          들어오는 돈은 선을 그어 따로 둔다. 같은 격자에 이어 붙이면 나가는 칸 옆에 나란히 서서
          부호가 반대라는 사실이 생김새에 전혀 드러나지 않는다 — 넷 중 하나만 더하는 쪽이다.
        */}
        <div className="mt-4 border-t border-grey-100 pt-3">
          <p className="mb-2 text-[12.5px] font-medium text-grey-500">여기서 들어오는 돈만큼 늦게 바닥나요</p>
          <div className="grid gap-3 sm:grid-cols-2">
            {linkIncome ? (
              <LinkedAmount
                label="그만둬도 들어오는 돈"
                provided={linkedIncome}
                source="소득"
                onOverride={() => override("income", linkedIncome)}
              />
            ) : (
              <MoneyInput
                label="그만둬도 들어오는 돈 (선택)"
                value={typedIncome}
                hint="실업급여·부업·월세처럼 퇴사 뒤에도 이어지는 것"
                onChange={setTypedIncome}
              />
            )}
          </div>
        </div>

        {cash !== null && monthlyBurn !== null && (
          <Result cash={cash} monthlyBurn={monthlyBurn} monthlyIncome={income} />
        )}
      </>
    );
  }

  return (
    <TaskToolPanel
      title="몇 달이나 버틸 수 있는지 계산해 보기"
      description="다음 수입까지의 공백을 개월 수로 바꿔 보면, 퇴사일을 언제로 잡을지가 훨씬 선명해져요."
      footnote={
        <>
          퇴직금과 건강보험료 변동은 넣지 않은 숫자예요. 여유를 조금 더 잡아 두세요.
          {missing.length > 0 && ` ${missing.join("·")} 모듈을 켜면 금액이 자동으로 들어와요.`}
        </>
      }
    >
      <WithAmounts providers={[CashOnHand, MonthlyFixedCost, ContinuingIncome]}>
        {([cash, fixedCost, income]) => body(cash, fixedCost, income)}
      </WithAmounts>
    </TaskToolPanel>
  );
}
