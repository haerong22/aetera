"use client";

import { Scale } from "lucide-react";
import { Card, CardHeader } from "@/components/ui/Card";
import { SummaryCard } from "@/components/ui/SummaryCard";
import { Spinner } from "@/components/ui/Spinner";
import { cn } from "@/components/ui/cn";
import { won } from "@/lib/money";
import {
  WithAmounts,
  missingProviders,
  useMonthlyFixedCost,
  useMonthlyIncome,
} from "@/modules/capabilityRegistry";
import { ModuleOffCard } from "./ModuleOffCard";

function Result({ income, fixedCost }: { income: number; fixedCost: number }) {
  /*
   * 소득이 0원이면 "모자란다"가 아니라 "아직 안 적었다"는 뜻이다. 등록한 소득은 1원 이상이라
   * 합계가 0이 되려면 항목이 없어야 한다. 방금 모듈을 켠 사람에게 첫 화면이 빨간 경고면
   * 고정지출을 줄이라는 말로 읽히는데, 정작 할 일은 소득을 적는 것이다.
   */
  if (income === 0) {
    return (
      <p className="py-2 text-[14px] text-grey-600">
        소득을 적으면 한 달에 얼마가 남는지 바로 보여드려요. 지금은 나가는 돈{" "}
        <b className="font-semibold text-grey-900">{won(fixedCost)}</b>만 알고 있어요.
      </p>
    );
  }

  const leftover = income - fixedCost;
  const short = leftover < 0;

  const share = Math.round((leftover / income) * 100);

  return (
    <>
      <SummaryCard className={cn(short && "border-danger/25 bg-danger/5")}>
        <div>
          <p className="text-[13px] font-medium text-grey-600">
            {short ? "이번 달 모자라는 돈" : "고정지출 빼고 남는 돈"}
          </p>
          <p
            className={cn(
              "mt-0.5 text-[28px] leading-tight font-bold tabular-nums",
              short ? "text-danger" : "text-primary",
            )}
          >
            {won(Math.abs(leftover))}
          </p>
        </div>
        <div className="text-right">
          <p className="text-[13px] font-medium text-grey-600 tabular-nums">
            {short ? `소득보다 ${-share}% 많아요` : `소득의 ${share}%`}
          </p>
          <p className="mt-0.5 text-[13px] text-grey-600 tabular-nums">
            {won(income)} − {won(fixedCost)}
          </p>
        </div>
      </SummaryCard>
      <Footnote />
    </>
  );
}

/** 무엇이 빠진 숫자인지. 뺄 셈이 있을 때만 뜻이 있어 [Result] 와 함께 움직인다. */
function Footnote() {
  return (
    <p className="mt-3 text-[12.5px] text-grey-500">
      식비·교통비처럼 매달 달라지는 지출은 빠져 있어요. 여기 남은 돈으로 생활하고 저축하는 셈이에요.
    </p>
  );
}

/**
 * 소득에서 고정지출을 뺀 나머지.
 *
 * **저축률이라고 부르지 않는다.** 고정지출에는 식비·교통비 같은 변동 지출이 없어서,
 * 여기 남은 돈이 그대로 통장에 쌓이지 않는다. 저축률이라 이름 붙이면 실제보다 훨씬
 * 잘하고 있다고 읽히므로, 무엇을 뺀 숫자인지를 이름과 각주가 그대로 말하게 둔다.
 *
 * 두 모듈이 다 켜져야 답할 수 있다. 하나만 켠 사람에게는 무엇을 더 켜야 하는지 말한다 —
 * 그냥 비워 두면 왜 안 보이는지 알 길이 없다.
 */
export function MonthlyLeftoverCard() {
  const MonthlyIncome = useMonthlyIncome();
  const MonthlyFixedCost = useMonthlyFixedCost();

  const missing = missingProviders([
    ["소득", MonthlyIncome],
    ["고정지출", MonthlyFixedCost],
  ]);

  if (missing.length > 0) {
    return <ModuleOffCard message={`${missing.join("·")} 모듈을 켜면 매달 얼마가 남는지 보여드려요`} />;
  }

  return (
    <Card>
      <CardHeader icon={<Scale size={16} />} title="이번 달 셈" />

      <WithAmounts providers={[MonthlyIncome, MonthlyFixedCost]}>
        {([income, fixedCost]) =>
          income === null || fixedCost === null ? (
            <div className="flex justify-center py-8">
              <Spinner />
            </div>
          ) : (
            <Result income={income.amount} fixedCost={fixedCost.amount} />
          )
        }
      </WithAmounts>
    </Card>
  );
}
