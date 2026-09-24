import { afterEach, beforeEach, vi } from "vitest";

/** 가로챈 요청 하나. */
export interface Sent {
  url: string;
  method: string;
  /** 보낸 본문(JSON 으로 읽은 것). 본문이 없으면 `undefined`. */
  body?: unknown;
}

/**
 * `fetch` 를 가로채고, **무엇을 보냈는지** 읽을 수 있게 한다.
 *
 * 시험마다 `spyOn` → `mockImplementation` → `restoreAllMocks` 를 손으로 적으면
 * 다섯 파일에 같은 여섯 줄이 생기고, 하나만 `restore` 를 빠뜨려도 그 뒤 시험들이
 * 앞선 가짜 응답을 그대로 받는다 — 그때는 **엉뚱한 곳이 실패한다.**
 *
 * 돌려주는 객체는 `beforeEach` 가 다시 채우므로, 시험 본문에서 그대로 읽으면 된다.
 */
export function stubFetch(respond: (request: Sent) => Response): { calls: Sent[]; last?: Sent } {
  const state: { calls: Sent[]; last?: Sent } = { calls: [] };

  beforeEach(() => {
    state.calls = [];
    state.last = undefined;

    vi.spyOn(globalThis, "fetch").mockImplementation(async (url, init) => {
      const sent: Sent = {
        url: String(url),
        method: init?.method ?? "GET",
        body: init?.body ? JSON.parse(String(init.body)) : undefined,
      };
      state.calls.push(sent);
      state.last = sent;
      return respond(sent);
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  return state;
}

/** 마지막으로 **본문을 실어 보낸** 요청의 본문. 조회가 뒤따라도 저장한 것을 놓치지 않는다. */
export function lastBody(state: { calls: Sent[] }): unknown {
  return [...state.calls].reverse().find((call) => call.body !== undefined)?.body;
}
