import { describe, expect, it } from "vitest";
import { freezeAt } from "@/test/clock";
import { formatExpiry, renewalStatus } from "./labels";
import type { Renewal } from "./api";

freezeAt("2026-09-17", "14:30");

/** 만기일과 알림일만 보므로 나머지는 최소로 채운다. */
function renewal(expiresAt: string, noticeDays = 30): Renewal {
  return {
    id: "r1",
    title: "실손보험",
    category: "INSURANCE",
    expiresAt,
    cycle: "YEARLY",
    noticeDays,
  } as Renewal;
}

describe("renewalStatus", () => {
  it("지난 만기는 expired", () => {
    expect(renewalStatus(renewal("2026-09-16"))).toBe("expired");
  });

  it("오늘 만기는 아직 expired 가 아니다", () => {
    expect(renewalStatus(renewal("2026-09-17"))).toBe("due");
  });

  it("알림일 안에 들면 due", () => {
    expect(renewalStatus(renewal("2026-10-17", 30))).toBe("due");
  });

  it("알림일 하루 밖이면 fine", () => {
    expect(renewalStatus(renewal("2026-10-18", 30))).toBe("fine");
  });

  /** 여권은 6개월 전, 보험은 한 달 전 — 항목마다 급해지는 시점이 다르다. */
  it("알림일은 항목마다 다르다", () => {
    expect(renewalStatus(renewal("2026-12-01", 30))).toBe("fine");
    expect(renewalStatus(renewal("2026-12-01", 180))).toBe("due");
  });
});

describe("formatExpiry", () => {
  it("오늘", () => {
    expect(formatExpiry(renewal("2026-09-17"))).toBe("오늘 만기");
  });

  it("앞으로", () => {
    expect(formatExpiry(renewal("2026-09-18"))).toBe("1일 남음");
  });

  it("지난 뒤", () => {
    expect(formatExpiry(renewal("2026-09-10"))).toBe("7일 지남");
  });
});
