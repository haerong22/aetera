"use client";

import { useState, type FormEvent } from "react";
import { Info, PiggyBank } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { MoneyInput } from "@/components/ui/MoneyInput";
import { cn } from "@/components/ui/cn";
import { won } from "@/lib/money";
import { estimateSeveranceTax, type SeveranceTax, type SeveranceTaxResult } from "./severanceTax";

const HOW_IT_IS_TAXED = [
  {
    title: "퇴직급여 원금 — 미뤄 뒀던 퇴직소득세",
    body: "회사가 IRP 로 넣어 줄 때는 세금을 떼지 않아요. 미룬 것뿐이라, 해지해 일시금으로 찾는 순간 전액 내게 됩니다.",
  },
  {
    title: "IRP 안에서 번 돈 — 기타소득세 16.5%",
    body: "운용수익과 세액공제를 받았던 내 납입금은 퇴직소득세가 아니라 기타소득세로 따로 매겨져요.",
  },
  {
    title: "55세 이후 연금으로 받으면 — 퇴직소득세의 70%",
    body: "나눠 받으면 세금이 깎여요. 연금 수령 11년차부터는 60%까지 내려갑니다.",
  },
];

function Row({
  label,
  amount,
  strong = false,
}: {
  label: string;
  amount: number;
  strong?: boolean;
}) {
  return (
    <div className={cn("flex items-baseline justify-between gap-3 py-1", strong && "font-bold")}>
      <span className={cn("text-[13.5px]", strong ? "text-grey-900" : "text-grey-600")}>{label}</span>
      <span className={cn("text-[14px] tabular-nums", strong ? "text-grey-900" : "text-grey-800")}>
        {won(amount)}
      </span>
    </div>
  );
}

function Result({ tax }: { tax: SeveranceTax }) {
  return (
    <div className="mt-4 flex flex-col gap-3">
      <p className="text-[13px] text-grey-500">
        근속 <span className="font-semibold text-grey-700">{tax.serviceYears}년</span> 기준으로 계산했어요.
      </p>

      <div className="rounded-(--radius-card) border border-grey-200 bg-white p-4">
        <p className="mb-2 text-[14px] font-bold text-grey-900">지금 해지해서 일시금으로 받으면</p>
        <Row label="퇴직소득세" amount={tax.incomeTax} />
        <Row label="지방소득세" amount={tax.localTax} />
        {tax.investmentGain > 0 && <Row label="기타소득세 (운용수익 16.5%)" amount={tax.otherIncomeTax} />}
        <div className="mt-2 border-t border-grey-100 pt-2">
          <Row label="세금 합계" amount={tax.totalTax} strong />
          <Row label="실수령액" amount={tax.netAmount} strong />
        </div>
      </div>

      <div className="rounded-(--radius-card) border border-success/30 bg-success-light p-4">
        <p className="mb-2 flex items-center gap-1.5 text-[14px] font-bold text-success">
          <PiggyBank size={16} aria-hidden />
          55세 이후 연금으로 나눠 받으면
        </p>
        <Row label="퇴직소득세 + 지방소득세" amount={tax.pensionTax} />
        <p className="mt-2 text-[13.5px] leading-relaxed text-grey-700">
          일시금보다 <span className="font-bold text-success">{won(tax.pensionSaving)}</span> 덜 냅니다.
          {tax.investmentGain > 0 && " 운용수익에 붙는 기타소득세는 이 비교에 넣지 않았어요."}
        </p>
      </div>

      <details className="rounded-(--radius-card) bg-grey-50 px-4 py-3">
        <summary className="cursor-pointer text-[13px] font-semibold text-grey-600">계산 과정 보기</summary>
        <div className="mt-2">
          <Row label="퇴직급여" amount={tax.severancePay} />
          <Row label="− 근속연수공제" amount={tax.serviceDeduction} />
          <Row label="= 환산급여 (÷ 근속연수 × 12)" amount={tax.convertedSalary} />
          <Row label="− 환산급여공제" amount={tax.convertedDeduction} />
          <Row label="= 과세표준" amount={tax.taxBase} />
          <Row label="= 퇴직소득세 (기본세율 → ÷ 12 × 근속연수)" amount={tax.incomeTax} />
        </div>
      </details>
    </div>
  );
}

/**
 * IRP 를 해지할 때 붙는 세금을 설명하고, 내 퇴직급여로 직접 계산해 본다.
 *
 * 계산은 이 브라우저 안에서 끝난다([estimateSeveranceTax]) —
 * 저장할 것도 남의 데이터를 읽을 것도 없는 산수라 금액이 기기 밖으로 나갈 이유가 없다.
 */
export function SeveranceTaxTool() {
  const [severancePay, setSeverancePay] = useState("");
  const [investmentGain, setInvestmentGain] = useState("");
  const [joinedOn, setJoinedOn] = useState("");
  const [leftOn, setLeftOn] = useState("");
  const [result, setResult] = useState<SeveranceTaxResult | null>(null);

  const ready = Number(severancePay || "0") > 0 && joinedOn !== "" && leftOn !== "";

  /**
   * 입력이 바뀌면 이전 결과를 지운다.
   *
   * 남겨 두면 6,000만원으로 계산한 세금이 1억이 적힌 칸 아래에 그대로 붙어 있게 된다 —
   * 화면의 숫자와 결과가 다른 상태를 사용자가 알아챌 방법이 없다.
   */
  function update(setter: (value: string) => void, value: string) {
    setResult(null);
    setter(value);
  }

  function submit(event: FormEvent) {
    event.preventDefault();
    if (!ready) return;
    setResult(
      estimateSeveranceTax({
        severancePay: Number(severancePay),
        joinedOn,
        leftOn,
        investmentGain: Number(investmentGain || "0"),
      }),
    );
  }

  return (
    <div className="rounded-(--radius-card) border border-grey-200 bg-grey-50 p-4 sm:p-5">
      <h3 className="text-[15px] font-bold text-grey-900">IRP 를 해지하면 세금이 이렇게 붙어요</h3>

      <ol className="mt-3 flex flex-col gap-2.5">
        {HOW_IT_IS_TAXED.map((item, index) => (
          <li key={item.title} className="flex gap-2.5">
            <span className="mt-0.5 flex size-5 shrink-0 items-center justify-center rounded-full bg-primary-light text-[11px] font-bold text-primary">
              {index + 1}
            </span>
            <span className="min-w-0">
              <span className="block text-[13.5px] font-semibold text-grey-800">{item.title}</span>
              <span className="mt-0.5 block text-[13px] leading-relaxed text-grey-600">{item.body}</span>
            </span>
          </li>
        ))}
      </ol>

      <form onSubmit={submit} className="mt-5 border-t border-grey-200 pt-4">
        <p className="text-[14px] font-bold text-grey-900">내 퇴직급여로 계산해 보기</p>

        <div className="mt-3 grid gap-3 sm:grid-cols-2">
          <MoneyInput
            label="퇴직급여 (IRP 로 들어온 금액)"
            value={severancePay}
            onChange={(digits) => update(setSeverancePay, digits)}
          />
          <MoneyInput
            label="IRP 운용수익 (선택)"
            value={investmentGain}
            hint="모르면 비워 두세요"
            onChange={(digits) => update(setInvestmentGain, digits)}
          />
          <Input
            label="입사일"
            type="date"
            value={joinedOn}
            max={leftOn || undefined}
            onChange={(event) => update(setJoinedOn, event.target.value)}
          />
          <Input
            label="퇴사일"
            type="date"
            value={leftOn}
            min={joinedOn || undefined}
            onChange={(event) => update(setLeftOn, event.target.value)}
          />
        </div>

        <Button type="submit" className="mt-4 w-full" disabled={!ready}>
          계산하기
        </Button>
      </form>

      {result?.ok === false && (
        <p role="alert" className="mt-3 text-[13px] text-danger">
          {result.message}
        </p>
      )}

      {result?.ok && <Result tax={result.tax} />}

      <p className="mt-4 flex items-start gap-2 text-[12px] leading-relaxed text-grey-500">
        <Info size={13} aria-hidden className="mt-0.5 shrink-0" />
        추정치예요. 비과세 퇴직급여, 임원 퇴직금 한도 초과분, 2012년 이전 근속분처럼 개인 이력을 알아야
        하는 항목은 빠져 있어요. 실제 금액은 원천징수영수증이나 국세청 모의계산으로 확인하세요.
      </p>
    </div>
  );
}
