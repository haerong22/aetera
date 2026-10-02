import { describe, expect, it, vi } from "vitest";
import { screen } from "@testing-library/react";
import { renderWithProviders } from "@/test/render";
import { jsonResponse } from "@/test/http";
import { failFetch, lastBody, stubFetch } from "@/test/stubFetch";
import { API_URL } from "@/lib/api-client";
import { localToday, toLocalDateIso } from "@/lib/date";
import { SpendingDialog } from "./SpendingDialog";
import type { SpendingBoard, SpendingRecord } from "../api";

/**
 * 이 다이얼로그가 지키는 것은 **0원을 적을 수 있는 것**이다.
 *
 * 다른 금액 화면들은 "0보다 커야 저장"인데 여기는 아니다. 한 푼도 안 쓴 달을 적어 둘 수
 * 있어야 **안 적은 달과 구분**되고, 그 구분이 평균을 가른다 — 안 적은 달은 평균에서
 * 빠지고, 0원이라 적은 달은 평균을 끌어내린다. 그 차이가 런웨이로 흘러간다.
 *
 * 공통 헬퍼(`describeMoneyDialog`)를 쓰지 않는다. 저쪽은 이름·주기 칸을 전제하는데
 * 이 모듈은 **달에 한 번 숫자 하나**라서 모양이 다르다.
 */

const board: SpendingBoard = { records: [], averagedMonths: 0 };

/*
 * 오늘을 얼리지 않는다. `freezeAt` 은 가짜 타이머를 켜는데 `userEvent` 는 진짜 타이머가
 * 있어야 입력을 흘려보낸다 — 켜 두면 타이핑이 아예 안 먹는다.
 *
 * 대신 기대값을 **화면 코드와 같은 헬퍼로** 만든다. 손으로 날짜를 적으면 내일 깨진다.
 */
const TODAY = toLocalDateIso(localToday());
const recordsUrl = (month: string) =>
  `${API_URL}/api/v1/modules/spending/records/${month}?today=${TODAY}`;

const sent = stubFetch(() => jsonResponse<SpendingBoard>(board));

const amountInput = () => screen.getByLabelText("그 달에 쓴 돈");
const saveButton = () => screen.getByRole("button", { name: "저장" });

function open(record?: SpendingRecord) {
  return renderWithProviders(
    <SpendingDialog open onClose={() => {}} month="2026-09-01" record={record} />,
  );
}

describe("제목", () => {
  // 달은 열 때 정해져 들어온다 — 안에서 또 고르게 하면 같은 선택을 두 번 하는 셈이다.
  it("어느 달을 적는지 제목에 있다", () => {
    open();

    expect(screen.getByText("2026년 9월 쓴 돈")).toBeInTheDocument();
  });
});

describe("금액", () => {
  it("비워 두면 저장할 수 없다", () => {
    open();

    expect(saveButton()).toBeDisabled();
  });

  /*
   * 이 시험이 이 화면의 이유다. "0보다 커야 저장"으로 만들면 0원인 달을 적을 수 없고,
   * 그러면 안 적은 달과 구분되지 않아 평균이 달라진다.
   */
  it("0원을 적을 수 있다", async () => {
    const { user } = open();

    await user.type(amountInput(), "0");

    expect(saveButton()).toBeEnabled();
  });

  it("적은 금액을 그 달로 보낸다", async () => {
    const { user } = open();

    await user.type(amountInput(), "480000");
    await user.click(saveButton());

    await vi.waitFor(() =>
      expect(sent.last).toMatchObject({
        url: recordsUrl("2026-09-01"),
        method: "PUT",
      }),
    );
    expect(lastBody(sent)).toEqual({ amount: 480000 });
  });
});

describe("메모", () => {
  it("적으면 함께 보낸다", async () => {
    const { user } = open();

    await user.type(amountInput(), "480000");
    await user.type(screen.getByLabelText("메모 (선택)"), "추석 선물");
    await user.click(saveButton());

    await vi.waitFor(() => expect(lastBody(sent)).toEqual({ amount: 480000, note: "추석 선물" }));
  });

  // 공백만 적은 메모를 보내면 서버가 다듬어 null 로 만든다 — 보내 봐야 소용없다.
  it("공백만 적었으면 보내지 않는다", async () => {
    const { user } = open();

    await user.type(amountInput(), "480000");
    await user.type(screen.getByLabelText("메모 (선택)"), "   ");
    await user.click(saveButton());

    await vi.waitFor(() => expect(lastBody(sent)).toEqual({ amount: 480000 }));
  });
});

describe("고치기", () => {
  const existing: SpendingRecord = { month: "2026-09-01", amount: 300_000, note: "병원비" };

  it("적어 둔 값이 칸에 들어 있다", () => {
    open(existing);

    expect(amountInput()).toHaveValue("300,000");
    expect(screen.getByLabelText("메모 (선택)")).toHaveValue("병원비");
  });

  // 아직 없는 달에 지우기를 두면 무엇을 지우는지 알 수 없다.
  it("새로 적을 때는 지우기가 없다", () => {
    open();

    expect(screen.queryByRole("button", { name: "이 달 기록 지우기" })).not.toBeInTheDocument();
  });

  it("고칠 때는 지울 수 있다", async () => {
    const { user } = open(existing);

    await user.click(screen.getByRole("button", { name: "이 달 기록 지우기" }));

    await vi.waitFor(() =>
      expect(sent.last).toMatchObject({
        url: recordsUrl("2026-09-01"),
        method: "DELETE",
      }),
    );
  });
});

describe("실패", () => {
  it("알리고 닫지 않는다", async () => {
    failFetch();
    const { user } = open();

    await user.type(amountInput(), "480000");
    await user.click(saveButton());

    expect(await screen.findByRole("alert")).toHaveTextContent("저장하지 못했어요");
    // 닫아 버리면 적어 둔 것이 사라져 처음부터 다시 적어야 한다.
    expect(amountInput()).toBeInTheDocument();
  });
});
