import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import { renderWithProviders } from "@/test/render";
import { jsonResponse } from "@/test/http";
import { SnapshotDialog } from "./SnapshotDialog";
import type { AssetBoard, AssetEntry } from "../api";

/**
 * 이 다이얼로그에서 손으로 잡은 버그가 둘이다 — 줄 키가 부딪혀 한 줄을 고치면 다른 줄이
 * 함께 바뀌었고, 목록이 다시 오면 편집 중이던 값이 지워졌다. 둘 다 여기서 잡는다.
 */

const entry = (name: string, amount: number): AssetEntry => ({
  name,
  category: "CASH",
  amount,
  // 현금이라 부호가 그대로다. 다이얼로그는 이 값을 읽지 않지만 타입이 요구한다.
  signedAmount: amount,
});

/** 저장 요청이 실어 보낸 것. 화면이 무엇을 보냈는지 보려면 이게 필요하다. */
let sent: unknown;

/** 저장·삭제가 돌려주는 화면 전체. 다이얼로그는 읽지 않지만 진짜와 같은 모양이어야 한다. */
const emptyBoard: AssetBoard = { entries: [], netWorth: 0, cashTotal: 0, history: [] };

beforeEach(() => {
  sent = undefined;
  vi.spyOn(globalThis, "fetch").mockImplementation(async (_url, init) => {
    if (init?.body) sent = JSON.parse(String(init.body));
    return jsonResponse(emptyBoard);
  });
});

afterEach(() => {
  vi.restoreAllMocks();
});

function open(entries: AssetEntry[] = [], existing = false) {
  const onClose = vi.fn();
  const rendered = renderWithProviders(
    <SnapshotDialog month="2026-09-01" entries={entries} existing={existing} onClose={onClose} />,
  );
  return { ...rendered, onClose };
}

/** 이름 칸들. 줄 순서대로 온다. */
const nameInputs = () => screen.getAllByLabelText("이름") as HTMLInputElement[];

describe("지난 기록 깔아 주기", () => {
  it("전달 계좌를 그대로 띄운다 — 매달 다시 적지 않게", () => {
    open([entry("통장", 1000), entry("적금", 2000)]);

    expect(nameInputs().map((input) => input.value)).toEqual(["통장", "적금"]);
  });

  it("지난 기록이 없으면 빈 줄 하나로 시작한다", () => {
    open([]);

    expect(nameInputs()).toHaveLength(1);
    expect(nameInputs()[0]).toHaveValue("");
  });
});

describe("줄 키", () => {
  /*
   * 예전에는 키가 `new-${index}-${drafts.length}` 였다. 두 줄에서 하나를 지우고 다시 추가하면
   * 앞서 쓴 키와 같은 값이 나와, 한 줄을 고칠 때 다른 줄까지 함께 바뀌었다.
   */
  it("지웠다 추가해도 줄이 서로 붙지 않는다", async () => {
    const { user } = open([entry("통장", 1000), entry("적금", 2000)]);

    // 둘째 줄을 지우고
    await user.click(screen.getByLabelText("적금 지우기"));
    // 새 줄을 추가한 뒤
    await user.click(screen.getByRole("button", { name: /줄 추가/ }));

    const inputs = nameInputs();
    await user.type(inputs[1], "새 계좌");

    // 첫 줄은 그대로여야 한다
    expect(nameInputs()[0]).toHaveValue("통장");
    expect(nameInputs()[1]).toHaveValue("새 계좌");
  });

  it("이름이 같은 두 줄도 따로 움직인다", async () => {
    const { user } = open([entry("통장", 1000), entry("통장", 2000)]);

    await user.clear(nameInputs()[0]);
    await user.type(nameInputs()[0], "주거래");

    expect(nameInputs()[0]).toHaveValue("주거래");
    expect(nameInputs()[1]).toHaveValue("통장");
  });
});

describe("저장", () => {
  it("이름이 빈 줄은 빼고 보낸다 — 지우기를 못 찾아 이름만 지우는 사람이 있다", async () => {
    const { user } = open([entry("통장", 1000), entry("적금", 2000)]);

    await user.clear(nameInputs()[1]);
    await user.click(screen.getByRole("button", { name: "저장" }));

    await waitFor(() => expect(sent).toBeDefined());
    expect(sent).toEqual({ entries: [{ name: "통장", category: "CASH", amount: 1000 }] });
  });

  it("이름 앞뒤 공백은 턴다", async () => {
    const { user } = open([entry("  통장  ", 1000)]);

    await user.click(screen.getByRole("button", { name: "저장" }));

    await waitFor(() => expect(sent).toBeDefined());
    expect((sent as { entries: { name: string }[] }).entries[0].name).toBe("통장");
  });

  it("모든 줄의 이름이 비면 저장할 수 없다", async () => {
    const { user } = open([entry("통장", 1000)]);

    await user.clear(nameInputs()[0]);

    expect(screen.getByRole("button", { name: "저장" })).toBeDisabled();
  });

  it("성공하면 닫는다", async () => {
    const { user, onClose } = open([entry("통장", 1000)]);

    await user.click(screen.getByRole("button", { name: "저장" }));

    await waitFor(() => expect(onClose).toHaveBeenCalled());
  });
});

describe("지우기", () => {
  it("이미 기록된 달에만 보인다", () => {
    open([entry("통장", 1000)], false);
    expect(screen.queryByRole("button", { name: /기록 지우기/ })).not.toBeInTheDocument();

    open([entry("통장", 1000)], true);
    expect(screen.getByRole("button", { name: /기록 지우기/ })).toBeInTheDocument();
  });
});
