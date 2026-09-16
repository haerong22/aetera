import { afterEach, beforeEach, vi } from "vitest";

/**
 * `"2026-09-17"` 을 **로컬** 시각의 `Date` 로.
 *
 * `new Date("2026-09-17")` 은 UTC 자정으로 읽혀, 한국에서 돌리면 9시가 된다.
 * 여기 함수들이 재는 것은 전부 사용자의 로컬 날짜라 그 차이가 하루를 만든다.
 */
export function localDate(isoDay: string, hhmm = "12:00"): Date {
  const [year, month, day] = isoDay.split("-").map(Number);
  const [hour, minute] = hhmm.split(":").map(Number);
  return new Date(year, month - 1, day, hour, minute);
}

/**
 * 이 파일의 시험이 도는 동안 "오늘"을 붙들어 둔다.
 *
 * 붙들지 않으면 자정 근처에 돌린 시험이 하루씩 흔들리고, 해가 바뀌면 조용히 깨진다.
 * 개별 시험이 다른 날을 봐야 하면 [localDate] 로 만들어 `vi.setSystemTime` 을 다시 부르면 된다.
 */
export function freezeAt(isoDay: string, hhmm?: string): void {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(localDate(isoDay, hhmm));
  });

  afterEach(() => {
    vi.useRealTimers();
  });
}
