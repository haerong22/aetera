import { describe, expect, it } from "vitest";
import { formatDay, untilLabel } from "./format";

describe("formatDay", () => {
  it("앞의 0 을 떼고 읽는다", () => {
    expect(formatDay("2026-09-05")).toBe("9월 5일");
  });

  it("연도는 말하지 않는다 — 부르는 쪽이 이미 말한다", () => {
    expect(formatDay("2025-12-31")).toBe("12월 31일");
  });
});

describe("untilLabel", () => {
  it("오늘과 내일은 날수 대신 말로", () => {
    expect(untilLabel(0)).toBe("오늘");
    expect(untilLabel(1)).toBe("내일");
  });

  it("그 뒤는 날수로", () => {
    expect(untilLabel(2)).toBe("2일 뒤");
    expect(untilLabel(180)).toBe("180일 뒤");
  });

  /** 앞으로 올 것에만 쓰지만, 지난 값이 들어와도 음수 날짜를 내놓지는 않는다. */
  it("음수는 오늘로 접는다", () => {
    expect(untilLabel(-3)).toBe("오늘");
  });
});
