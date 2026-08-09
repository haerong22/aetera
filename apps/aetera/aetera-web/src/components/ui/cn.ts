/** 조건부 클래스 결합. clsx 의 최소 구현 — 의존성 하나를 아낀다. */
export function cn(...values: Array<string | false | null | undefined>): string {
  return values.filter(Boolean).join(" ");
}
