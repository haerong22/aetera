import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { screen } from "@testing-library/react";
import { renderWithProviders } from "@/test/render";
import { failFetch, stubFetch } from "@/test/stubFetch";
import { API_URL } from "@/lib/api-client";
import { ExportSection } from "./ExportSection";

/**
 * 내보내기는 **브라우저 쪽 일이 많아서** 조용히 깨지기 쉽다.
 *
 * 응답을 JSON 으로 파싱하지 않고 블롭 그대로 내려받고, `Content-Disposition` 에서 파일 이름을
 * 뽑고, 다 쓴 블롭 주소를 놓아 준다. 어느 한 칸이 틀려도 화면에는 아무 표시가 나지 않는다 —
 * 파일이 `aetera.json` 이 아닌 이름으로 떨어지거나, 내용이 한 줄로 뭉개지거나,
 * 블롭이 메모리에 남는다. 전부 사용자가 알아차리기 어렵다.
 */

/** 서버가 내려보내는 모양. 들여쓰기가 있는 것이 중요하다 — 그게 살아 있어야 한다. */
const PAYLOAD = '{\n  "profile": {\n    "nickname": "홍길동"\n  }\n}';

const sent = stubFetch(
  () =>
    new Response(PAYLOAD, {
      headers: {
        "Content-Type": "application/json",
        "Content-Disposition": 'attachment; filename="aetera-20260925.json"',
      },
    }),
);

/**
 * 만들어진 블롭과 눌린 링크를 붙잡는다.
 *
 * jsdom 의 `createObjectURL` 은 실제로 동작하므로 흉내 낼 필요가 없다. 감싸는 이유는
 * **무엇을 넘겼는지 보려는 것**이다 — 블롭의 내용과 놓아 주었는지가 이 화면의 핵심이다.
 */
const captured = {
  blobs: [] as Blob[],
  revoked: [] as string[],
  clicked: [] as HTMLAnchorElement[],
};

beforeEach(() => {
  captured.blobs = [];
  captured.revoked = [];
  captured.clicked = [];

  const createObjectURL = URL.createObjectURL.bind(URL);
  vi.spyOn(URL, "createObjectURL").mockImplementation((obj) => {
    if (obj instanceof Blob) captured.blobs.push(obj);
    return createObjectURL(obj);
  });
  vi.spyOn(URL, "revokeObjectURL").mockImplementation((url) => {
    captured.revoked.push(url);
  });
  // 실제로 누르면 jsdom 이 이동을 시도한다. 눌린 링크만 받아 둔다.
  vi.spyOn(HTMLAnchorElement.prototype, "click").mockImplementation(function (this: HTMLAnchorElement) {
    captured.clicked.push(this);
  });
});

afterEach(() => {
  vi.restoreAllMocks();
});

const downloadButton = () => screen.getByRole("button", { name: "내려받기" });

async function download() {
  const { user } = renderWithProviders(<ExportSection />);
  await user.click(downloadButton());
  await vi.waitFor(() => expect(captured.clicked).toHaveLength(1));
  return captured.clicked[0];
}

describe("내려받기", () => {
  it("내보내기 주소로 요청한다", async () => {
    await download();

    expect(sent.last).toMatchObject({ url: `${API_URL}/api/v1/me/export`, method: "GET" });
  });

  /*
   * 서버가 날짜를 넣어 이름을 정해 준다. 화면이 제 이름을 붙이면 여러 번 내려받은 파일을
   * 구분할 수 없어, 내려받기 폴더에 `aetera (3).json` 이 쌓인다.
   */
  it("서버가 정한 파일 이름을 쓴다", async () => {
    const link = await download();

    expect(link.download).toBe("aetera-20260925.json");
  });

  /*
   * 이 화면이 `apiFetch` 가 아니라 `apiFetchRaw` 를 쓰는 이유다. 파싱했다가 다시
   * 문자열로 만들면 줄 모양이 사라져 사람이 열어 볼 수 없는 한 줄이 된다.
   */
  it("서버가 보낸 바이트를 그대로 담는다", async () => {
    await download();

    expect(await captured.blobs[0].text()).toBe(PAYLOAD);
  });

  /*
   * 놓아 주지 않으면 블롭이 탭이 닫힐 때까지 메모리에 남는다. 내 데이터 전부를 담은
   * 파일이라 작지 않고, 여러 번 누르면 그만큼 쌓인다.
   */
  it("다 쓴 블롭 주소를 놓아 준다", async () => {
    const link = await download();

    await vi.waitFor(() => expect(captured.revoked).toHaveLength(1));
    expect(captured.revoked[0]).toBe(link.href);
  });
});

describe("파일 이름을 못 읽을 때", () => {
  // 헤더가 없거나 모양이 다르면 이름 없는 파일이 떨어진다. 그보다는 기본 이름이 낫다.
  it("헤더가 없으면 기본 이름을 쓴다", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response(PAYLOAD));

    const link = await download();

    expect(link.download).toBe("aetera.json");
  });

  it("헤더 모양이 달라도 기본 이름을 쓴다", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(PAYLOAD, { headers: { "Content-Disposition": "attachment" } }),
    );

    const link = await download();

    expect(link.download).toBe("aetera.json");
  });
});

describe("실패", () => {
  it("알리고 내려받지 않는다", async () => {
    failFetch();
    const { user } = renderWithProviders(<ExportSection />);

    await user.click(downloadButton());

    expect(await screen.findByRole("alert")).toHaveTextContent("내려받지 못했어요");
    expect(captured.clicked).toHaveLength(0);
  });

  // 실패한 뒤에도 다시 누를 수 있어야 한다 — `busy` 가 풀리지 않으면 영영 막힌다.
  it("실패한 뒤에도 다시 시도할 수 있다", async () => {
    failFetch();
    const { user } = renderWithProviders(<ExportSection />);

    await user.click(downloadButton());
    await screen.findByRole("alert");

    expect(downloadButton()).toBeEnabled();
  });
});
