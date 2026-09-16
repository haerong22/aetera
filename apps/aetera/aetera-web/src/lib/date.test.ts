import { describe, expect, it, vi } from "vitest";
import { freezeAt, localDate } from "@/test/clock";
import { daysUntil, fromLocalDateIso, isoFromToday, nearestIsoOccurrence, toLocalDateIso } from "./date";

freezeAt("2026-09-17", "14:30");

describe("toLocalDateIso", () => {
  it("한 자리 달과 날에 0을 채운다", () => {
    expect(toLocalDateIso(new Date(2026, 0, 5))).toBe("2026-01-05");
  });

  it("UTC 가 아니라 로컬 날짜를 쓴다", () => {
    // 로컬 자정 직후. UTC 로 읽으면 전날이 되는 시각대가 있다.
    expect(toLocalDateIso(new Date(2026, 2, 1, 0, 30))).toBe("2026-03-01");
  });
});

describe("fromLocalDateIso", () => {
  it("로컬 자정으로 되돌린다", () => {
    const date = fromLocalDateIso("2026-03-01");
    expect(date.getFullYear()).toBe(2026);
    expect(date.getMonth()).toBe(2);
    expect(date.getDate()).toBe(1);
    expect(date.getHours()).toBe(0);
  });

  it("문자열로 갔다 오면 같은 날이다", () => {
    expect(toLocalDateIso(fromLocalDateIso("2026-12-31"))).toBe("2026-12-31");
  });
});

describe("daysUntil", () => {
  it("오늘은 0", () => {
    expect(daysUntil("2026-09-17")).toBe(0);
  });

  it("내일은 1, 어제는 -1", () => {
    expect(daysUntil("2026-09-18")).toBe(1);
    expect(daysUntil("2026-09-16")).toBe(-1);
  });

  it("오늘이 몇 시든 날짜 차이만 센다", () => {
    vi.setSystemTime(localDate("2026-09-17", "23:59"));
    expect(daysUntil("2026-09-18")).toBe(1);
  });

  it("달과 해를 넘어간다", () => {
    expect(daysUntil("2026-10-17")).toBe(30);
    expect(daysUntil("2027-09-17")).toBe(365);
  });
});

describe("isoFromToday", () => {
  it("말일을 넘기면 다음 달로 굴러간다", () => {
    vi.setSystemTime(localDate("2026-01-30"));
    expect(isoFromToday(3)).toBe("2026-02-02");
  });

  it("음수면 뒤로 간다", () => {
    expect(isoFromToday(-17)).toBe("2026-08-31");
  });
});

describe("nearestIsoOccurrence", () => {
  it("올해 것이 가까우면 올해", () => {
    expect(nearestIsoOccurrence("12-31")).toBe("2026-12-31");
  });

  /**
   * 1월에 연말정산을 하는 사람이 "다음 12월 31일"을 받으면 1년 뒤 날짜가 된다.
   * 방금 지나간 12월 31일이 그 사람이 정산하는 해다.
   */
  it("막 지나간 해가 더 가까우면 지난해를 준다", () => {
    vi.setSystemTime(localDate("2026-01-15"));
    expect(nearestIsoOccurrence("12-31")).toBe("2025-12-31");
  });

  it("내년 것이 더 가까우면 내년", () => {
    vi.setSystemTime(localDate("2026-12-20"));
    expect(nearestIsoOccurrence("01-05")).toBe("2027-01-05");
  });
});
