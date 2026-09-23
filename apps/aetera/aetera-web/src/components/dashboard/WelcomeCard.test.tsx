import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import { renderWithProviders } from "@/test/render";
import { errorResponse, jsonResponse } from "@/test/http";
import { WelcomeCard } from "./WelcomeCard";
import type { ModuleSummary } from "@/lib/types";

/**
 * 첫 화면의 분기라 눈으로 보기 전에 여기서 굳힌다.
 *
 * 특히 "하나 켜도 카드가 남는가" — 셋을 나란히 권해 놓고 하나 고르면 나머지를 치우는
 * 동작을 한 번 고쳤던 자리다.
 */

const module = (id: string, displayName: string, enabled = false): ModuleSummary => ({
  id,
  displayName,
  description: `${displayName} 설명`,
  category: "TOOL",
  version: "1.0.0",
  enabled,
});

/** 서버가 주는 모듈 목록. 시험마다 바꿔 끼운다. */
let modules: ModuleSummary[] = [];

beforeEach(() => {
  modules = [
    module("schedule", "일정"),
    module("expense", "고정지출"),
    module("renewal", "만기 관리"),
    module("goal", "목표"),
  ];

  vi.spyOn(globalThis, "fetch").mockImplementation(async (url, init) => {
    // 켜기 요청이면 그 모듈을 켠 목록을 돌려준다 — 실제 서버와 같은 모양이다.
    const enabling = String(url).match(/modules\/([^/]+)\/enablement/);
    if (enabling && init?.method === "POST") {
      const target = modules.find((it) => it.id === enabling[1]);
      // 없는 모듈이면 서버도 404 를 준다. 단언으로 터뜨리면 원인이 흐려진다.
      if (!target) return errorResponse(404, 4040301, "존재하지 않는 모듈입니다.");
      return jsonResponse({ ...target, enabled: true });
    }
    return jsonResponse(modules);
  });
});

afterEach(() => {
  vi.restoreAllMocks();
});

/** 목록에 보이는 "켜기" 버튼들. */
const enableButtons = () => screen.queryAllByRole("button", { name: "켜기" });

describe("첫 제안", () => {
  it("켜자마자 화면이 달라지는 셋을 권한다", async () => {
    renderWithProviders(<WelcomeCard />);

    await waitFor(() => expect(enableButtons()).toHaveLength(3));
    expect(screen.getByText("일정")).toBeInTheDocument();
    expect(screen.getByText("고정지출")).toBeInTheDocument();
    expect(screen.getByText("만기 관리")).toBeInTheDocument();
  });

  // 가이드는 그 상황인 사람에게만 뜻이 있어 첫 제안에서 뺐다.
  it("제안하지 않은 모듈은 목록에 없다", async () => {
    renderWithProviders(<WelcomeCard />);

    await waitFor(() => expect(enableButtons()).toHaveLength(3));
    expect(screen.queryByText("목표")).not.toBeInTheDocument();
  });

  it("이름과 설명은 서버가 준 값을 쓴다", async () => {
    modules = [module("schedule", "일정 관리"), module("expense", "고정지출"), module("renewal", "만기")];

    renderWithProviders(<WelcomeCard />);

    await screen.findByText("일정 관리");
    expect(screen.getByText("일정 관리 설명")).toBeInTheDocument();
  });

  it("이미 켠 것은 권하지 않는다", async () => {
    modules = [
      module("schedule", "일정", true),
      module("expense", "고정지출"),
      module("renewal", "만기 관리"),
    ];

    renderWithProviders(<WelcomeCard />);

    await waitFor(() => expect(enableButtons()).toHaveLength(2));
    expect(screen.queryByText("일정")).not.toBeInTheDocument();
  });
});

describe("켜기", () => {
  it("누르면 그 줄만 빠지고 나머지는 남는다", async () => {
    const { user } = renderWithProviders(<WelcomeCard />);

    await waitFor(() => expect(enableButtons()).toHaveLength(3));
    // 일정을 켠다
    modules = modules.map((it) => (it.id === "schedule" ? { ...it, enabled: true } : it));
    await user.click(enableButtons()[0]);

    await waitFor(() => expect(enableButtons()).toHaveLength(2));
    expect(screen.getByText("고정지출")).toBeInTheDocument();
    expect(screen.getByText("만기 관리")).toBeInTheDocument();
  });

  /*
   * 이 시험은 `renderWithProviders` 가 재시도를 꺼 둔 것에 기댄다. 켜져 있으면 기본 세 번을
   * 기다리다 타임아웃으로 끝나고, 그러면 "실패를 알리는가"가 아니라 "느린가"를 재게 된다.
   */
  it("실패하면 말한다 — 없으면 잘못 눌렀다고 여긴다", async () => {
    const { user } = renderWithProviders(<WelcomeCard />);
    await waitFor(() => expect(enableButtons()).toHaveLength(3));

    vi.spyOn(globalThis, "fetch").mockResolvedValue(errorResponse(500, 5000001, "서버 오류"));

    await user.click(enableButtons()[0]);

    expect(await screen.findByRole("alert")).toHaveTextContent("켜지 못했어요");
  });
});
