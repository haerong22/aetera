/**
 * 주어진 아이디 순서대로 정렬한다. 목록에 없는 것은 뒤로 보낸다 —
 * 새로 배포된 모듈이 순서 목록에 없다고 사라지면 안 된다.
 *
 * 사이드바와 모듈 스토어가 같은 규칙을 써야 두 화면의 순서가 어긋나지 않는다.
 */
export function sortByIdOrder<T>(
  items: readonly T[],
  order: readonly string[],
  idOf: (item: T) => string,
): T[] {
  const rank = new Map(order.map((id, index) => [id, index]));
  return [...items].sort(
    (a, b) =>
      (rank.get(idOf(a)) ?? Number.MAX_SAFE_INTEGER) - (rank.get(idOf(b)) ?? Number.MAX_SAFE_INTEGER),
  );
}
