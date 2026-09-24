import { describe, expect, it } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import { renderWithProviders } from "@/test/render";
import { jsonResponse } from "@/test/http";
import { lastBody, stubFetch } from "@/test/stubFetch";
import { describeMoneyDialog } from "@/test/moneyDialog";
import { ExpenseDialog } from "./ExpenseDialog";
import type { Expense, ExpenseBoard } from "../api";

const emptyBoard: ExpenseBoard = { items: [], monthlyTotal: 0, yearlyTotal: 0 };

const rent: Expense = {
  id: "e1",
  title: "월세",
  category: "HOUSING",
  amount: 700_000,
  cycle: "MONTHLY",
  yearlyAmount: 8_400_000,
};

const http = stubFetch(() => jsonResponse(emptyBoard));

describeMoneyDialog<Expense>({
  label: "고정지출",
  amountLabel: "금액",
  render: (onClose, item) => (
    <ExpenseDialog open onClose={onClose} expense={item} />
  ),
  existing: rent,
  lastSent: () => lastBody(http),
});

describe("저장", () => {
  it("고른 분류와 주기를 그대로 보낸다", async () => {
    const { user } = renderWithProviders(<ExpenseDialog open onClose={() => {}} />);

    await user.type(screen.getByLabelText("이름"), "자동차보험");
    await user.type(screen.getByLabelText("금액"), "600000");
    await user.selectOptions(screen.getByLabelText("분류"), "INSURANCE");
    await user.selectOptions(screen.getByLabelText("주기"), "YEARLY");
    await user.click(screen.getByRole("button", { name: "저장" }));

    await waitFor(() => expect(lastBody(http)).toBeDefined());
    expect(lastBody(http)).toMatchObject({
      title: "자동차보험",
      amount: 600_000,
      category: "INSURANCE",
      cycle: "YEARLY",
    });
  });

  it("메모를 적으면 함께 보낸다", async () => {
    const { user } = renderWithProviders(<ExpenseDialog open onClose={() => {}} expense={rent} />);

    await user.type(screen.getByLabelText("메모 (선택)"), "2027년 3월 만기");
    await user.click(screen.getByRole("button", { name: "저장" }));

    await waitFor(() => expect(lastBody(http)).toBeDefined());
    expect((lastBody(http) as { memo?: string }).memo).toBe("2027년 3월 만기");
  });
});

describe("금액 표기", () => {
  // 0 을 하나 더 붙인 실수를 자릿수를 세지 않고도 알아차리게 한다.
  it("큰 숫자를 조·억·만으로 읽어 준다", async () => {
    const { user } = renderWithProviders(<ExpenseDialog open onClose={() => {}} />);

    await user.type(screen.getByLabelText("금액"), "60000000");

    expect(screen.getByText("6,000만원")).toBeInTheDocument();
  });

  it("쉼표를 넣어 보여준다", async () => {
    const { user } = renderWithProviders(<ExpenseDialog open onClose={() => {}} />);

    await user.type(screen.getByLabelText("금액"), "700000");

    expect(screen.getByLabelText("금액")).toHaveValue("700,000");
  });
});
