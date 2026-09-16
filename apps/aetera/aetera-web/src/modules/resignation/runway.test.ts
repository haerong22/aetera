import { describe, expect, it, vi } from "vitest";
import { freezeAt, localDate } from "@/test/clock";
import { formatRunsOut, runwayMonths } from "./runway";

freezeAt("2026-09-17");

describe("runwayMonths", () => {
  it("나가는 돈으로 가진 돈을 나눈다", () => {
    expect(runwayMonths(10_000_000, 1_000_000, 0)).toEqual({
      kind: "months",
      months: 10,
      netBurn: 1_000_000,
    });
  });

  it("들어오는 돈만큼 늦게 바닥난다", () => {
    // 200만 나가고 100만 들어오면 실제로 줄어드는 건 100만
    expect(runwayMonths(10_000_000, 2_000_000, 1_000_000)).toEqual({
      kind: "months",
      months: 10,
      netBurn: 1_000_000,
    });
  });

  it("들어오는 돈이 나가는 돈을 넘으면 바닥나지 않는다", () => {
    expect(runwayMonths(10_000_000, 1_000_000, 1_500_000)).toEqual({ kind: "never" });
  });

  it("딱 맞아떨어져도 바닥나지 않는다", () => {
    expect(runwayMonths(10_000_000, 1_000_000, 1_000_000)).toEqual({ kind: "never" });
  });

  it("가진 돈이 없으면 답할 수 없다", () => {
    expect(runwayMonths(0, 1_000_000, 0)).toEqual({ kind: "unknown" });
  });

  it("나가는 돈이 없으면 답할 수 없다", () => {
    expect(runwayMonths(10_000_000, 0, 0)).toEqual({ kind: "unknown" });
  });

  /** 가진 돈이 없는 쪽이 먼저다 — 둘 다 0 이어도 "바닥나지 않는다"고 하면 안 된다. */
  it("가진 돈도 나가는 돈도 없으면 unknown", () => {
    expect(runwayMonths(0, 0, 0)).toEqual({ kind: "unknown" });
  });
});

describe("formatRunsOut", () => {
  /**
   * 한때 `setMonth(+floor(months))` 로 더하면서 1일로 옮겼더니, "오늘이 31일"이라는 사실과
   * 소수부가 함께 날아가 **바닥나는 달이 한 달 앞당겨졌다.** 그때 손으로 찾은 다섯 사례를
   * 그대로 남긴다 — 다섯 중 넷이 틀렸었다.
   */
  const cases: [string, string, number, string][] = [
    ["달 중간", "2026-01-15", 3, "2026년 4월"],
    ["말일에서 3개월", "2026-01-31", 3, "2026년 5월"],
    ["소수부가 다음 달로 넘김", "2026-01-15", 0.8, "2026년 2월"],
    ["해를 넘김", "2026-11-20", 2, "2027년 1월"],
    ["0.5개월은 보름 뒤", "2026-03-01", 0.5, "2026년 3월"],
  ];

  it.each(cases)("%s", (_name, today, months, expected) => {
    vi.setSystemTime(localDate(today, "09:00"));
    expect(formatRunsOut(months)).toBe(expected);
  });

  it("긴 기간도 해를 제대로 넘긴다", () => {
    expect(formatRunsOut(24)).toBe("2028년 9월");
  });
});
