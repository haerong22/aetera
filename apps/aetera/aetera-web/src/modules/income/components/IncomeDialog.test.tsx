import { describe, expect, it } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import { renderWithProviders } from "@/test/render";
import { jsonResponse } from "@/test/http";
import { lastBody, stubFetch } from "@/test/stubFetch";
import { describeMoneyDialog } from "@/test/moneyDialog";
import { IncomeDialog } from "./IncomeDialog";
import type { Income, IncomeBoard } from "../api";

const emptyBoard: IncomeBoard = {
  items: [],
  monthlyTotal: 0,
  yearlyTotal: 0,
  monthlyContinuing: 0,
};

const salary: Income = {
  id: "i1",
  title: "월급",
  category: "SALARY",
  amount: 3_200_000,
  cycle: "MONTHLY",
  yearlyAmount: 38_400_000,
  continuesAfterLeaving: false,
};

const http = stubFetch(() => jsonResponse(emptyBoard));

describeMoneyDialog<Income>({
  label: "소득",
  amountLabel: "실수령액",
  render: (onClose, item) => (
    <IncomeDialog open onClose={onClose} income={item} />
  ),
  existing: salary,
  lastSent: () => lastBody(http),
});

/** 소득에만 있는 것: 분류가 "몇 달 버티나" 계산을 바꾼다. */
describe("분류 안내", () => {
  function open(income?: Income) {
    return renderWithProviders(<IncomeDialog open onClose={() => {}} income={income} />);
  }

  it("월급은 그만두면 멈추는 돈이라고 알린다", () => {
    open(salary);

    expect(screen.getByText(/월급은 일을 그만두면 멈추는 돈/)).toBeInTheDocument();
  });

  it("나머지 분류는 이어지는 돈이라고 알린다", async () => {
    const { user } = open(salary);

    await user.selectOptions(screen.getByLabelText("분류"), "SIDE");

    expect(screen.getByText(/그만둬도 이어지는 돈/)).toBeInTheDocument();
  });

  // 계산을 바꾸는 유일한 칸이라, 고른 순간 그 자리에서 말해야 한다.
  it("고르자마자 바뀐다 — 저장을 기다리지 않는다", async () => {
    const { user } = open();

    expect(screen.getByText(/월급은 일을 그만두면 멈추는 돈/)).toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText("분류"), "PENSION");
    expect(screen.getByText(/그만둬도 이어지는 돈/)).toBeInTheDocument();
  });
});

describe("저장", () => {
  it("고른 분류와 주기를 그대로 보낸다", async () => {
    const { user } = renderWithProviders(<IncomeDialog open onClose={() => {}} />);

    await user.type(screen.getByLabelText("이름"), "배당");
    await user.type(screen.getByLabelText("실수령액"), "300000");
    await user.selectOptions(screen.getByLabelText("분류"), "FINANCIAL");
    await user.selectOptions(screen.getByLabelText("주기"), "QUARTERLY");
    await user.click(screen.getByRole("button", { name: "저장" }));

    await waitFor(() => expect(lastBody(http)).toBeDefined());
    expect(lastBody(http)).toMatchObject({
      title: "배당",
      amount: 300_000,
      category: "FINANCIAL",
      cycle: "QUARTERLY",
    });
  });

  it("빈 메모는 보내지 않는다 — 서버가 null 로 접는 값을 굳이 실어 보낼 이유가 없다", async () => {
    const { user } = renderWithProviders(<IncomeDialog open onClose={() => {}} />);

    await user.type(screen.getByLabelText("이름"), "월급");
    await user.type(screen.getByLabelText("실수령액"), "3200000");
    await user.click(screen.getByRole("button", { name: "저장" }));

    await waitFor(() => expect(lastBody(http)).toBeDefined());
    expect((lastBody(http) as { memo?: string }).memo).toBeUndefined();
  });
});
