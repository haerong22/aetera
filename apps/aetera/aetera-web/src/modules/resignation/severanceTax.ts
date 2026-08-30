/**
 * 퇴직급여를 일시금으로 받을 때 세금이 얼마인지 추정한다.
 *
 * 퇴직소득세는 다른 소득과 합치지 않고 홀로 매긴다. 오래 다닐수록 세 부담이 줄도록
 * **근속연수로 나눴다가 다시 곱하는** 연분연승법을 쓴다 — 같은 1억이라도 3년 만에 받은 것과
 * 20년 만에 받은 것의 세금이 다른 이유다.
 *
 * 서버에 보내지 않는다. 저장할 것도, 남의 데이터를 읽을 것도 없는 산수라서
 * **퇴직급여 금액이 기기 밖으로 나갈 이유가 없다.**
 *
 * **추정치다.** 비과세 퇴직급여, 임원 퇴직금 한도 초과분, 2012년 이전 근속분에 적용되는
 * 옛 계산식처럼 개인 이력을 알아야 하는 항목은 넣지 않았다. 화면은 이 사실을 함께 보여준다.
 */

import { inKoreanUnits } from "@/lib/money";

/** 사람이 실수로 0을 몇 개 더 붙였을 때 말이 되는 답을 내놓지 않기 위한 상한. */
const MAX_AMOUNT = 1_000_000_000_000;

/**
 * 근속 기간의 상한.
 *
 * 금액과 같은 종류의 방어다. `<input type="date">` 는 연도 네 자리를 그대로 받으므로
 * 2016 을 치려다 0016 이 되면 근속 2000년이 되고, 근속연수공제가 수십억이 되어
 * **"세금 0원"이 아무 경고 없이 나온다.** 틀린 답을 조용히 내놓느니 되묻는다.
 */
const MAX_SERVICE_YEARS = 60;

/** 운용수익을 일시금으로 빼면 기타소득세 15% + 지방소득세 1.5%. */
const OTHER_INCOME_TAX_PERMILLE = 165;

/** 55세 이후 연금으로 나눠 받으면 퇴직소득세의 70%만 낸다(11년차부터는 60%). */
const PENSION_TAX_PERCENT = 70;

export interface SeveranceTax {
  severancePay: number;
  serviceYears: number;
  serviceDeduction: number;
  convertedSalary: number;
  convertedDeduction: number;
  taxBase: number;
  /** 퇴직소득세(산출세액). */
  incomeTax: number;
  /** 지방소득세 — 퇴직소득세의 10%. */
  localTax: number;
  investmentGain: number;
  /** 운용수익을 일시금으로 뺄 때의 기타소득세(16.5%). */
  otherIncomeTax: number;
  totalTax: number;
  netAmount: number;
  /** 같은 금액을 55세 이후 연금으로 나눠 받을 때의 세금. 운용수익 몫은 빼고 비교한다. */
  pensionTax: number;
  /** 연금으로 받으면 아끼는 금액. 일시금과의 차이가 곧 "해지 비용"이다. */
  pensionSaving: number;
}

/**
 * 예외 대신 판정 결과를 돌려준다 — 잘못된 입력은 사용자가 고칠 수 있는 상태이지
 * 프로그램이 멈출 일이 아니다. 화면은 `ok` 만 보고 결과나 안내 중 하나를 그린다.
 */
export type SeveranceTaxResult =
  | { ok: true; tax: SeveranceTax }
  | { ok: false; message: string };

export function estimateSeveranceTax({
  severancePay,
  joinedOn,
  leftOn,
  investmentGain = 0,
}: {
  severancePay: number;
  joinedOn: string;
  leftOn: string;
  investmentGain?: number;
}): SeveranceTaxResult {
  // 한도는 숫자가 아니라 읽히는 말로 알린다 — "1000000000000원"은 사람이 못 읽는다.
  const limit = inKoreanUnits(MAX_AMOUNT);

  if (!Number.isFinite(severancePay) || severancePay <= 0 || severancePay > MAX_AMOUNT) {
    return { ok: false, message: `퇴직급여는 1원 이상 ${limit} 이하여야 해요.` };
  }
  if (!Number.isFinite(investmentGain) || investmentGain < 0 || investmentGain > MAX_AMOUNT) {
    return { ok: false, message: `운용수익은 0원 이상 ${limit} 이하여야 해요.` };
  }
  if (leftOn < joinedOn) {
    return { ok: false, message: "퇴사일이 입사일보다 빨라요." };
  }

  const years = serviceYears(joinedOn, leftOn);
  if (years > MAX_SERVICE_YEARS) {
    return {
      ok: false,
      message: `근속 기간이 ${MAX_SERVICE_YEARS}년을 넘어요. 입사일과 퇴사일의 연도를 확인해 주세요.`,
    };
  }

  const serviceDeduction = deductionForService(years);

  // 근속연수공제가 퇴직급여보다 크면 낼 세금이 없다. 음수로 흘러가지 않게 여기서 끊는다.
  // 12 를 먼저 곱한다 — 나누고 곱하면 버려진 원 단위가 12배로 벌어진다.
  const convertedSalary = Math.floor((Math.max(0, severancePay - serviceDeduction) * 12) / years);
  const convertedDeduction = deductionForConverted(convertedSalary);
  const taxBase = Math.max(0, convertedSalary - convertedDeduction);

  const incomeTax = Math.floor(progressiveTax(taxBase) / 12) * years;
  const localTax = Math.floor(incomeTax / 10);
  const otherIncomeTax = Math.floor((investmentGain * OTHER_INCOME_TAX_PERMILLE) / 1000);
  const lumpSumTax = incomeTax + localTax;
  const pensionTax = Math.floor((lumpSumTax * PENSION_TAX_PERCENT) / 100);

  return {
    ok: true,
    tax: {
      severancePay,
      serviceYears: years,
      serviceDeduction,
      convertedSalary,
      convertedDeduction,
      taxBase,
      incomeTax,
      localTax,
      investmentGain,
      otherIncomeTax,
      totalTax: lumpSumTax + otherIncomeTax,
      netAmount: severancePay + investmentGain - lumpSumTax - otherIncomeTax,
      pensionTax,
      pensionSaving: lumpSumTax - pensionTax,
    },
  };
}

/**
 * 근속연수. **1년 미만은 1년으로 올린다** — 하루를 더 다녀도 한 해를 채운 것으로 본다.
 * 올림이라 근속연수공제가 커지고, 결과적으로 세금이 줄어드는 방향이다.
 *
 * `Date` 를 쓰지 않고 문자열을 쪼갠다. `new Date("2016-03-01")` 은 UTC 로 읽히는데
 * 브라우저의 시간대에 따라 하루가 밀려 근속연수가 한 해 달라질 수 있다.
 */
function serviceYears(joinedOn: string, leftOn: string): number {
  const [joinedYear, joinedMonth, joinedDay] = joinedOn.split("-").map(Number);
  const [leftYear, leftMonth, leftDay] = leftOn.split("-").map(Number);

  const beforeAnniversary =
    leftMonth < joinedMonth || (leftMonth === joinedMonth && leftDay < joinedDay);
  const fullYears = leftYear - joinedYear - (beforeAnniversary ? 1 : 0);
  const onAnniversary = leftMonth === joinedMonth && leftDay === joinedDay;

  return Math.max(1, onAnniversary ? fullYears : fullYears + 1);
}

/** 근속연수공제. 오래 다닐수록 한 해당 공제액이 커진다. */
function deductionForService(years: number): number {
  if (years <= 5) return 1_000_000 * years;
  if (years <= 10) return 5_000_000 + 2_000_000 * (years - 5);
  if (years <= 20) return 15_000_000 + 2_500_000 * (years - 10);
  return 40_000_000 + 3_000_000 * (years - 20);
}

/** 환산급여공제. 환산급여가 클수록 공제율이 낮아진다. */
function deductionForConverted(converted: number): number {
  if (converted <= 8_000_000) return converted;
  if (converted <= 70_000_000) return 8_000_000 + Math.floor(((converted - 8_000_000) * 60) / 100);
  if (converted <= 100_000_000) return 45_200_000 + Math.floor(((converted - 70_000_000) * 55) / 100);
  if (converted <= 300_000_000) return 61_700_000 + Math.floor(((converted - 100_000_000) * 45) / 100);
  return 151_700_000 + Math.floor(((converted - 300_000_000) * 35) / 100);
}

/**
 * 종합소득 기본세율(누진공제 방식). `[세율%, 누진공제액]`.
 *
 * 위의 두 공제표와 같은 if 사슬로 둔다. 표를 배열로 두면 마지막 구간에 상한을 적어야 하고,
 * 그러면 "못 찾았을 때"를 다루는 죽은 가지가 생긴다.
 */
function rateFor(base: number): [number, number] {
  if (base <= 14_000_000) return [6, 0];
  if (base <= 50_000_000) return [15, 1_260_000];
  if (base <= 88_000_000) return [24, 5_760_000];
  if (base <= 150_000_000) return [35, 15_440_000];
  if (base <= 300_000_000) return [38, 19_940_000];
  if (base <= 500_000_000) return [40, 25_940_000];
  if (base <= 1_000_000_000) return [42, 35_940_000];
  return [45, 65_940_000];
}

function progressiveTax(base: number): number {
  const [ratePercent, progressiveDeduction] = rateFor(base);
  return Math.max(0, Math.floor((base * ratePercent) / 100) - progressiveDeduction);
}
