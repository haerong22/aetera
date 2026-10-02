import { describe, expect, it } from "vitest";
import { screen } from "@testing-library/react";
import { renderWithProviders } from "@/test/render";
import { jsonResponse } from "@/test/http";
import { stubFetch } from "@/test/stubFetch";
import { SpendingPage } from "./SpendingPage";
import type { SpendingBoard, SpendingRecord } from "./api";

/**
 * 이 화면이 지키는 것은 **평균을 몇 달로 냈는지 밝히는 것**이다.
 *
 * 두 달치 평균을 "평소 한 달에"라고만 적으면, 그 숫자를 받아 쓰는 퇴사 준비의 런웨이가
 * 그만큼 흔들리는데 보는 사람은 알 수 없다. 숫자보다 **그 숫자를 얼마나 믿을지**가 먼저다.
 *
 * 권하는 달이 지난 달인 것도 본다 — 달이 끝나야 쓴 돈이 확정되고 명세서도 그때 나온다.
 */

let board: SpendingBoard = { records: [], averagedMonths: 0 };

stubFetch(() => jsonResponse<SpendingBoard>(board));

/** 지금 기준의 지난 달·이번 달 이름. 손으로 적으면 다음 달에 깨진다. */
function monthName(monthsAgo: number): string {
  const date = new Date();
  date.setDate(1);
  date.setMonth(date.getMonth() - monthsAgo);
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월`;
}

/** 서버가 보내는 모양의 달 문자열. */
function monthIso(monthsAgo: number): string {
  const date = new Date();
  date.setDate(1);
  date.setMonth(date.getMonth() - monthsAgo);
  const month = String(date.getMonth() + 1).padStart(2, "0");
  return `${date.getFullYear()}-${month}-01`;
}

describe("아직 적은 달이 없을 때", () => {
  it("무엇을 하면 되는지 말해 준다", async () => {
    board = { records: [], averagedMonths: 0 };

    renderWithProviders(<SpendingPage />);

    expect(await screen.findByText("아직 적은 달이 없어요")).toBeInTheDocument();
  });

  // 달이 끝나야 쓴 돈이 확정되고 카드 명세서도 그때 나온다.
  it("이번 달이 아니라 지난 달을 권한다", async () => {
    board = { records: [], averagedMonths: 0 };

    renderWithProviders(<SpendingPage />);

    expect(await screen.findByRole("button", { name: `${monthName(1)} 적기` })).toBeInTheDocument();
  });

  it("평균은 보여 주지 않는다", async () => {
    board = { records: [], averagedMonths: 0 };

    renderWithProviders(<SpendingPage />);

    await screen.findByText("아직 적은 달이 없어요");
    expect(screen.queryByText("평소 한 달에")).not.toBeInTheDocument();
  });
});

describe("평균", () => {
  it("몇 달치인지 함께 적는다", async () => {
    // 목록의 금액을 평균과 다르게 둔다 — 같으면 "500,000원"이 요약과 목록 둘에 떠서 집지 못한다.
    board = {
      records: [{ month: monthIso(1), amount: 620_000 }],
      monthlyAverage: 500_000,
      averagedMonths: 1,
    };

    renderWithProviders(<SpendingPage />);

    expect(await screen.findByText("500,000원")).toBeInTheDocument();
    expect(screen.getByText(/최근 1개월 평균/)).toBeInTheDocument();
  });

  /*
   * 한두 달치를 "평소"로 읽으면 안 된다. 이 경고가 없으면 사용자는 그 숫자를
   * 여섯 달치와 똑같이 믿는다.
   */
  it("달이 적으면 더 쌓으라고 말한다", async () => {
    board = {
      records: [{ month: monthIso(1), amount: 500_000 }],
      monthlyAverage: 500_000,
      averagedMonths: 2,
    };

    renderWithProviders(<SpendingPage />);

    expect(await screen.findByText(/달이 쌓이면 더 정확해져요/)).toBeInTheDocument();
  });

  it("달이 넉넉하면 경고를 치운다", async () => {
    board = {
      records: [{ month: monthIso(1), amount: 500_000 }],
      monthlyAverage: 500_000,
      averagedMonths: 6,
    };

    renderWithProviders(<SpendingPage />);

    await screen.findByText(/최근 6개월 평균/);
    expect(screen.queryByText(/달이 쌓이면/)).not.toBeInTheDocument();
  });
});

/*
 * 어느 갈래인지는 **서버가 정한다**(`comparison`). 그래서 이 시험들은 문턱값을 재지 않는다 —
 * 그건 모델 시험(`SpendingComparisonTest`)의 몫이다. 여기서 보는 것은 **서버가 말한 갈래를
 * 화면이 그대로 따르는가**다. 화면이 제 문턱값을 들고 있으면 이 시험이 잡는다.
 */
describe("평소와 견주기", () => {
  function boardWith(record: SpendingRecord): SpendingBoard {
    return { records: [record], monthlyAverage: 500_000, averagedMonths: 2 };
  }

  it("MORE 면 많다고 적는다", async () => {
    board = boardWith({
      month: monthIso(1),
      amount: 620_000,
      versusAverage: 120_000,
      comparison: "MORE",
    });

    renderWithProviders(<SpendingPage />);

    expect(await screen.findByText("평소보다 120,000원 많아요")).toBeInTheDocument();
  });

  it("LESS 면 적다고 적는다", async () => {
    board = boardWith({
      month: monthIso(1),
      amount: 380_000,
      versusAverage: -120_000,
      comparison: "LESS",
    });

    renderWithProviders(<SpendingPage />);

    expect(await screen.findByText("평소보다 120,000원 적어요")).toBeInTheDocument();
  });

  /*
   * 금액 차이가 커 보여도 서버가 SIMILAR 라 했으면 그대로 따른다 — 화면이 제 문턱값으로
   * 다시 판단하면 타임라인과 갈린다.
   */
  it("SIMILAR 면 차이가 커도 비슷하다고 적는다", async () => {
    board = boardWith({
      month: monthIso(1),
      amount: 900_000,
      versusAverage: 400_000,
      comparison: "SIMILAR",
    });

    renderWithProviders(<SpendingPage />);

    expect(await screen.findByText("평소와 비슷해요")).toBeInTheDocument();
  });

  // 견줄 평균이 없는 달은 아무 말도 붙이지 않는다.
  it("갈래가 없으면 견주지 않는다", async () => {
    // 요약의 평균과 다른 금액을 둔다 — 같으면 "500,000원"이 둘에 떠서 집지 못한다.
    board = boardWith({ month: monthIso(1), amount: 620_000 });

    renderWithProviders(<SpendingPage />);

    await screen.findByText("620,000원");
    expect(screen.queryByText(/평소보다/)).not.toBeInTheDocument();
  });
});

describe("권하는 달", () => {
  it("지난 달을 이미 적었으면 이번 달을 권한다", async () => {
    board = {
      records: [{ month: monthIso(1), amount: 500_000 }],
      monthlyAverage: 500_000,
      averagedMonths: 1,
    };

    renderWithProviders(<SpendingPage />);

    expect(await screen.findByRole("button", { name: `${monthName(0)} 적기` })).toBeInTheDocument();
  });
});
