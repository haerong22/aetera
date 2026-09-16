import { describe, expect, it } from "vitest";
import { estimateSeveranceTax } from "./severanceTax";

/**
 * 이 계산은 한때 Kotlin 원본과 104개 사례로 맞춰 본 적이 있지만, 그 원본은 지워졌다.
 * 지금 세율표를 잘못 건드려도 알려 주는 것이 아무것도 없다 — 그래서 **구간 경계**를 못 박는다.
 *
 * 값은 계산기의 현재 동작을 그대로 굳힌 것이다. 국세청 고시와 맞는지를 보는 시험이 아니라,
 * 고칠 때 **모르는 새 달라지지 않게** 잡아 두는 시험이다. 세법이 바뀌면 여기 숫자도 함께 바꾼다.
 */

/** 시험마다 되풀이되는 입력을 줄인다. 기본은 1억을 10년 만에 받는 경우. */
function tax(over: Partial<Parameters<typeof estimateSeveranceTax>[0]> = {}) {
  const result = estimateSeveranceTax({
    severancePay: 100_000_000,
    joinedOn: "2016-03-01",
    leftOn: "2026-03-01",
    ...over,
  });
  if (!result.ok) throw new Error(`계산이 거절됨: ${result.message}`);
  return result.tax;
}

describe("근속연수", () => {
  it("입사 기념일에 그만두면 딱 그 해수다", () => {
    expect(tax({ joinedOn: "2016-03-01", leftOn: "2026-03-01" }).serviceYears).toBe(10);
  });

  it("하루를 더 다니면 한 해를 올린다", () => {
    expect(tax({ joinedOn: "2016-03-01", leftOn: "2026-03-02" }).serviceYears).toBe(11);
  });

  it("기념일 하루 전이면 아직 그 해를 못 채웠다", () => {
    expect(tax({ joinedOn: "2016-03-01", leftOn: "2026-02-28" }).serviceYears).toBe(10);
  });

  it("1년을 못 채워도 1년으로 본다", () => {
    expect(tax({ joinedOn: "2026-01-01", leftOn: "2026-01-02" }).serviceYears).toBe(1);
  });

  it("같은 날 들어와 같은 날 나가도 1년이다", () => {
    expect(tax({ joinedOn: "2026-01-01", leftOn: "2026-01-01" }).serviceYears).toBe(1);
  });
});

describe("근속연수공제 구간", () => {
  /** 구간이 바뀌는 해에서만 확인한다 — 안쪽은 같은 식이 이어진다. */
  const cases: [number, number][] = [
    [1, 1_000_000],
    [5, 5_000_000],
    [6, 7_000_000],
    [10, 15_000_000],
    [11, 17_500_000],
    [20, 40_000_000],
    [21, 43_000_000],
  ];

  it.each(cases)("%i년이면 %i원", (years, expected) => {
    // 3월 1일에 들어와 기념일에 나가면 근속연수가 정확히 years 가 된다.
    expect(tax({ joinedOn: `${2026 - years}-03-01`, leftOn: "2026-03-01" }).serviceDeduction).toBe(
      expected,
    );
  });
});

describe("연분연승", () => {
  it("같은 금액이라도 오래 다녔으면 세금이 적다", () => {
    const short = tax({ joinedOn: "2023-03-01" }).incomeTax;
    const long = tax({ joinedOn: "2006-03-01" }).incomeTax;
    expect(long).toBeLessThan(short);
  });

  it("환산급여는 근속연수공제를 뺀 뒤 12를 곱해 나눈다", () => {
    const result = tax();
    // (1억 − 1500만) × 12 ÷ 10
    expect(result.convertedSalary).toBe(102_000_000);
  });

  it("근속연수공제가 퇴직급여보다 크면 세금이 0원이다", () => {
    const result = tax({ severancePay: 1_000_000, joinedOn: "1996-03-01" });
    expect(result.convertedSalary).toBe(0);
    expect(result.incomeTax).toBe(0);
    expect(result.totalTax).toBe(0);
  });
});

describe("지방소득세", () => {
  it("퇴직소득세의 10%를 버림으로 매긴다", () => {
    const result = tax();
    expect(result.localTax).toBe(Math.floor(result.incomeTax / 10));
  });
});

describe("운용수익", () => {
  it("일시금으로 빼면 16.5%", () => {
    expect(tax({ investmentGain: 10_000_000 }).otherIncomeTax).toBe(1_650_000);
  });

  it("없으면 0원이고 퇴직소득세에는 섞이지 않는다", () => {
    const withGain = tax({ investmentGain: 10_000_000 });
    const without = tax();
    expect(without.otherIncomeTax).toBe(0);
    expect(withGain.incomeTax).toBe(without.incomeTax);
  });

  it("실수령액은 퇴직급여와 운용수익에서 세금을 전부 뺀 값이다", () => {
    const result = tax({ investmentGain: 10_000_000 });
    expect(result.netAmount).toBe(
      result.severancePay + result.investmentGain - result.incomeTax - result.localTax - result.otherIncomeTax,
    );
  });
});

describe("연금 비교", () => {
  it("연금으로 받으면 퇴직소득세의 70%만 낸다", () => {
    const result = tax();
    const lumpSum = result.incomeTax + result.localTax;
    expect(result.pensionTax).toBe(Math.floor((lumpSum * 70) / 100));
    expect(result.pensionSaving).toBe(lumpSum - result.pensionTax);
  });

  it("운용수익의 기타소득세는 비교에 넣지 않는다", () => {
    const withGain = tax({ investmentGain: 50_000_000 });
    const without = tax();
    expect(withGain.pensionTax).toBe(without.pensionTax);
  });
});

describe("되묻는 입력", () => {
  const rejected = (over: Parameters<typeof estimateSeveranceTax>[0]) => {
    const result = estimateSeveranceTax(over);
    expect(result.ok).toBe(false);
    return result.ok ? "" : result.message;
  };

  const base = { severancePay: 100_000_000, joinedOn: "2016-03-01", leftOn: "2026-03-01" };

  it("퇴직급여가 0원이면 거절한다", () => {
    expect(rejected({ ...base, severancePay: 0 })).toContain("퇴직급여는");
  });

  it("퇴직급여가 1조를 넘으면 거절한다", () => {
    expect(rejected({ ...base, severancePay: 1_000_000_000_001 })).toContain("퇴직급여는");
  });

  it("운용수익이 음수면 거절한다", () => {
    expect(rejected({ ...base, investmentGain: -1 })).toContain("운용수익은");
  });

  it("퇴사일이 입사일보다 빠르면 거절한다", () => {
    expect(rejected({ ...base, joinedOn: "2026-03-01", leftOn: "2016-03-01" })).toBe(
      "퇴사일이 입사일보다 빨라요.",
    );
  });

  /**
   * 연도를 잘못 친 경우. 0016 년 입사로 읽히면 근속연수공제가 수십억이 되어
   * "세금 0원"이 아무 경고 없이 나온다 — 조용히 틀리느니 되묻는다.
   */
  it("근속 60년을 넘으면 연도를 확인하라고 한다", () => {
    expect(rejected({ ...base, joinedOn: "0016-03-01" })).toContain("근속 기간이");
  });
});
