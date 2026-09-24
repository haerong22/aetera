import { describe, expect, it } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import type { ReactElement } from "react";
import { renderWithProviders } from "./render";

/**
 * 소득과 고정지출 다이얼로그가 똑같이 지켜야 하는 것들.
 *
 * 두 화면은 부호만 반대일 뿐 같은 모양이다 — 이름·금액·주기·분류·메모에 저장과 삭제.
 * 같은 시험을 두 벌 적으면 한쪽만 고쳐져 조용히 갈린다.
 *
 * **각자의 다른 점은 여기 넣지 않는다.** 소득의 "분류가 계산을 바꾼다"는 안내처럼
 * 한쪽에만 있는 것은 그 파일에서 따로 본다.
 */
export function describeMoneyDialog<T extends { title: string; amount: number }>(options: {
  /** "소득" 처럼 화면에 쓰이는 이름. 제목을 찾는 데 쓴다. */
  label: string;
  /** 금액 칸의 라벨. 소득은 "실수령액", 고정지출은 "금액". */
  amountLabel: string;
  /** 다이얼로그를 그린다. `item` 이 없으면 추가, 있으면 수정 모드. */
  render: (onClose: () => void, item?: T) => ReactElement;
  /** 수정 모드에 넣을 기존 항목. */
  existing: T;
  /** 저장 요청이 실어 보낸 본문. */
  lastSent: () => unknown;
}) {
  const { label, amountLabel, render, existing, lastSent } = options;

  function open(item?: T) {
    return renderWithProviders(render(() => {}, item));
  }

  describe(`${label} 다이얼로그 — 공통`, () => {
    it("추가와 수정의 제목이 다르다", () => {
      const added = open();
      expect(added.getByText(`${label} 추가`)).toBeInTheDocument();
      added.unmount();

      const edited = open(existing);
      expect(edited.getByText(`${label} 수정`)).toBeInTheDocument();
      expect(edited.queryByText(`${label} 추가`)).not.toBeInTheDocument();
    });

    it("수정이면 기존 값을 깔아 준다", () => {
      open(existing);

      expect(screen.getByLabelText("이름")).toHaveValue(existing.title);
      expect(screen.getByLabelText(amountLabel)).toHaveValue(existing.amount.toLocaleString("ko-KR"));
    });

    it("이름이 비면 저장할 수 없다", () => {
      open();

      expect(screen.getByRole("button", { name: "저장" })).toBeDisabled();
    });

    it("금액이 0이면 저장할 수 없다 — 이름만으로는 셈이 안 된다", async () => {
      const { user } = open();

      await user.type(screen.getByLabelText("이름"), "월세");

      expect(screen.getByRole("button", { name: "저장" })).toBeDisabled();
    });

    it("이름과 금액이 차면 저장할 수 있다", async () => {
      const { user } = open();

      await user.type(screen.getByLabelText("이름"), "월세");
      await user.type(screen.getByLabelText(amountLabel), "700000");

      expect(screen.getByRole("button", { name: "저장" })).toBeEnabled();
    });

    it("이름 앞뒤 공백은 턴다", async () => {
      const { user } = open();

      await user.type(screen.getByLabelText("이름"), "  월세  ");
      await user.type(screen.getByLabelText(amountLabel), "700000");
      await user.click(screen.getByRole("button", { name: "저장" }));

      await waitFor(() => expect(lastSent()).toBeDefined());
      expect((lastSent() as { title: string }).title).toBe("월세");
    });

    it("삭제는 수정할 때만 보인다", () => {
      const added = open();
      expect(added.queryByRole("button", { name: /삭제/ })).not.toBeInTheDocument();
      added.unmount();

      const edited = open(existing);
      expect(edited.getByRole("button", { name: /삭제/ })).toBeInTheDocument();
    });
  });
}
