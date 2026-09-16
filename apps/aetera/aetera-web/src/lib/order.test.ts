import { describe, expect, it } from "vitest";
import { sortByIdOrder } from "./order";

const idOf = (item: { id: string }) => item.id;
const ids = (items: { id: string }[]) => items.map(idOf);

describe("sortByIdOrder", () => {
  const items = [{ id: "a" }, { id: "b" }, { id: "c" }];

  it("준 순서대로 늘어놓는다", () => {
    expect(ids(sortByIdOrder(items, ["c", "a", "b"], idOf))).toEqual(["c", "a", "b"]);
  });

  /** 새로 배포된 모듈이 순서 목록에 없다고 사라지면 안 된다. */
  it("목록에 없는 것은 뒤로 보내되 버리지 않는다", () => {
    expect(ids(sortByIdOrder(items, ["c"], idOf))).toEqual(["c", "a", "b"]);
  });

  it("목록이 비어도 전부 남는다", () => {
    expect(ids(sortByIdOrder(items, [], idOf))).toEqual(["a", "b", "c"]);
  });

  it("목록에만 있고 실제로 없는 아이디는 무시한다", () => {
    expect(ids(sortByIdOrder(items, ["zzz", "b"], idOf))).toEqual(["b", "a", "c"]);
  });

  it("원본을 건드리지 않는다", () => {
    const original = [...items];
    sortByIdOrder(items, ["c", "b", "a"], idOf);
    expect(items).toEqual(original);
  });
});
