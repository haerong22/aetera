import { describe, expect, it, vi } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import { renderWithProviders } from "@/test/render";
import { errorResponse, jsonResponse } from "@/test/http";
import { lastBody, stubFetch } from "@/test/stubFetch";
import { GoalDialog } from "./GoalDialog";
import type { Goal } from "../api";

/**
 * 다른 다이얼로그들과 달리 금액이 아니라 **목표치와 단위**를 받는다.
 * 기간을 바꾸면 쌓인 진행이 사라진다는 점도 여기만 있다.
 */

const weekly: Goal = {
  id: "g1",
  title: "운동하기",
  period: "WEEKLY",
  target: 3,
  unit: "회",
  progress: 1,
  periodStart: "2026-09-21",
  achieved: false,
};

const http = stubFetch(() => jsonResponse([weekly]));

function open(goal?: Goal) {
  const onClose = vi.fn();
  return { onClose, ...renderWithProviders(<GoalDialog open onClose={onClose} goal={goal} />) };
}

describe("기본값", () => {
  it("추가는 주 3회로 시작한다 — 가장 흔한 모양이라 그대로 저장해도 말이 된다", () => {
    open();

    expect(screen.getByLabelText("목표치")).toHaveValue(3);
    expect(screen.getByLabelText("단위")).toHaveValue("회");
  });

  it("수정이면 기존 값을 깔아 준다", () => {
    open({ ...weekly, title: "책 읽기", target: 10, unit: "쪽", period: "MONTHLY" });

    expect(screen.getByLabelText("무엇을 할까요")).toHaveValue("책 읽기");
    expect(screen.getByLabelText("목표치")).toHaveValue(10);
    expect(screen.getByLabelText("단위")).toHaveValue("쪽");
  });
});

describe("기간을 바꾸면", () => {
  // 진행이 0 으로 돌아가는 건 되돌릴 수 없으니, 누르기 전에 알려야 한다.
  it("진행이 사라진다고 미리 알린다", async () => {
    const { user } = open(weekly);

    expect(screen.queryByText(/0 부터 다시 시작/)).not.toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText("기간"), "MONTHLY");

    expect(screen.getByText(/0 부터 다시 시작/)).toBeInTheDocument();
  });

  it("원래대로 돌리면 안내도 걷힌다", async () => {
    const { user } = open(weekly);

    await user.selectOptions(screen.getByLabelText("기간"), "MONTHLY");
    await user.selectOptions(screen.getByLabelText("기간"), "WEEKLY");

    expect(screen.queryByText(/0 부터 다시 시작/)).not.toBeInTheDocument();
  });

  // 새로 만드는 목표에는 잃을 진행이 없다.
  it("추가할 때는 알리지 않는다", async () => {
    const { user } = open();

    await user.selectOptions(screen.getByLabelText("기간"), "MONTHLY");

    expect(screen.queryByText(/0 부터 다시 시작/)).not.toBeInTheDocument();
  });
});

describe("저장", () => {
  it("이름이 비면 저장할 수 없다", () => {
    open();

    expect(screen.getByRole("button", { name: "저장" })).toBeDisabled();
  });

  it("공백만 친 이름도 막는다", async () => {
    const { user } = open();

    await user.type(screen.getByLabelText("무엇을 할까요"), "   ");

    expect(screen.getByRole("button", { name: "저장" })).toBeDisabled();
  });

  it("적은 대로 보낸다", async () => {
    const { user } = open();

    await user.type(screen.getByLabelText("무엇을 할까요"), "물 마시기");
    await user.clear(screen.getByLabelText("목표치"));
    await user.type(screen.getByLabelText("목표치"), "8");
    await user.clear(screen.getByLabelText("단위"));
    await user.type(screen.getByLabelText("단위"), "잔");
    await user.click(screen.getByRole("button", { name: "저장" }));

    await waitFor(() => expect(lastBody(http)).toBeDefined());
    expect(lastBody(http)).toMatchObject({ title: "물 마시기", target: 8, unit: "잔", period: "WEEKLY" });
  });

  // 단위를 비우면 서버도 화면도 그 자리를 비운다 — 없는 단위를 지어내지 않는다.
  it("단위를 지우면 보내지 않는다", async () => {
    const { user } = open();

    await user.type(screen.getByLabelText("무엇을 할까요"), "명상하기");
    await user.clear(screen.getByLabelText("단위"));
    await user.click(screen.getByRole("button", { name: "저장" }));

    await waitFor(() => expect(lastBody(http)).toBeDefined());
    expect((lastBody(http) as { unit?: string }).unit).toBeUndefined();
  });

  it("성공하면 닫는다", async () => {
    const { user, onClose } = open(weekly);

    await user.click(screen.getByRole("button", { name: "저장" }));

    await waitFor(() => expect(onClose).toHaveBeenCalled());
  });

  it("실패하면 말한다", async () => {
    const { user } = open(weekly);
    vi.spyOn(globalThis, "fetch").mockResolvedValue(errorResponse(500, 5000001));

    await user.click(screen.getByRole("button", { name: "저장" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("저장하지 못했어요");
  });
});

describe("삭제", () => {
  it("수정할 때만 보인다", () => {
    open();
    expect(screen.queryByRole("button", { name: /삭제/ })).not.toBeInTheDocument();

    open(weekly);
    expect(screen.getByRole("button", { name: /삭제/ })).toBeInTheDocument();
  });

  it("누르면 닫는다", async () => {
    const { user, onClose } = open(weekly);

    await user.click(screen.getByRole("button", { name: /삭제/ }));

    await waitFor(() => expect(onClose).toHaveBeenCalled());
  });
});
