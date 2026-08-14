/**
 * 날짜를 **로컬 달력 기준** 문자열로 바꾼다.
 *
 * `toISOString()` 을 쓰면 안 된다 — UTC 로 옮겨 적기 때문에 한국(UTC+9)에서 자정 직후에는
 * 하루 전 날짜가 나온다. 사용자가 달력에서 고른 날은 사용자의 달력 그대로여야 한다.
 *
 * 모듈(일정, 가이드)이 각자 갖고 있으면 한쪽에서 틀렸을 때 다른 쪽도 같이 틀린다.
 * 모듈끼리는 서로를 참조하지 않으므로 공용 자리인 여기에 둔다.
 */
export function toLocalDateIso(date: Date): string {
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  return `${date.getFullYear()}-${month}-${day}`;
}
