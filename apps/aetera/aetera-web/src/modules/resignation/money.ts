export const won = (amount: number) => `${amount.toLocaleString("ko-KR")}원`;

const UNITS: [number, string][] = [
  [1_000_000_000_000, "조"],
  [100_000_000, "억"],
  [10_000, "만"],
];

/**
 * 큰 숫자를 조·억·만으로 읽어 준다. 60000000 옆에 "6,000만원"이 같이 있으면
 * 0 을 하나 더 붙인 실수를 자릿수를 세지 않고도 알아차린다.
 *
 * 만원 미만은 `null` — "3,000원"을 "3,000원"이라고 한 번 더 말해 줄 이유가 없다.
 * 만 단위 아래는 버린다. 확인용이지 정확한 표기가 아니다.
 */
export function inKoreanUnits(amount: number): string | null {
  if (amount < 10_000) return null;

  const parts: string[] = [];
  let rest = amount;
  for (const [size, name] of UNITS) {
    const count = Math.floor(rest / size);
    if (count > 0) {
      parts.push(`${count.toLocaleString("ko-KR")}${name}`);
      rest -= count * size;
    }
  }
  return parts.length > 0 ? `${parts.join(" ")}원` : null;
}
