import { describe, expect, it } from "vitest";
import { inKoreanUnits, won } from "./money";

describe("won", () => {
  it("세 자리마다 쉼표를 넣는다", () => {
    expect(won(48_200_000)).toBe("48,200,000원");
  });

  it("음수와 0도 그대로", () => {
    expect(won(0)).toBe("0원");
    expect(won(-1_200_000)).toBe("-1,200,000원");
  });
});

describe("inKoreanUnits", () => {
  it("만원 미만은 말하지 않는다", () => {
    expect(inKoreanUnits(3_000)).toBeNull();
    expect(inKoreanUnits(9_999)).toBeNull();
  });

  it("만 단위부터 읽어 준다", () => {
    expect(inKoreanUnits(10_000)).toBe("1만원");
    expect(inKoreanUnits(60_000_000)).toBe("6,000만원");
  });

  it("빈 자리는 건너뛴다", () => {
    // 1억 + 0만 — "1억 0만원"이라고 하지 않는다.
    expect(inKoreanUnits(100_000_000)).toBe("1억원");
  });

  it("여러 단위를 붙여 읽는다", () => {
    expect(inKoreanUnits(123_450_000)).toBe("1억 2,345만원");
    expect(inKoreanUnits(1_000_000_000_000)).toBe("1조원");
  });

  it("만 단위 아래는 버린다 — 확인용이지 정확한 표기가 아니다", () => {
    expect(inKoreanUnits(12_345)).toBe("1만원");
  });
});
