import { beforeEach, describe, expect, it, vi } from "vitest";
import { screen } from "@testing-library/react";
import { renderWithProviders } from "@/test/render";
import { errorResponse, jsonResponse } from "@/test/http";
import { failFetch, lastBody, stubFetch } from "@/test/stubFetch";
import { API_URL } from "@/lib/api-client";
import { NotificationSection } from "./NotificationSection";
import type { NotificationPreference } from "@/lib/notifications";

/**
 * 알림 설정은 값 왕복이 전부지만, **끈 사람에게 시각을 묻지 않는 것**이 이 화면의 판단이다.
 *
 * 고를 수 있으면 뭔가 오는 줄 안다. 끄고도 "받을 시각"이 남아 있으면 사용자는 꺼진 줄
 * 모른 채 메일을 기다리고, 안 오면 고장이라고 여긴다.
 *
 * 시각을 **내 시간대 기준**으로 고르는 것도 여기서 드러난다 — 서버 잡이 매시 돌면서
 * 각자에게 몇 시인지 보고 고르므로, 화면은 시간대를 셈하지 않는다.
 */

let preference: NotificationPreference = { enabled: true, sendHour: 9 };

/** 저장만 실패시킨다. 읽기는 살려 둬야 저장 오류 문구에 닿는다 — 아래 설명 참고. */
let saveFails = false;

const sent = stubFetch(({ method, body }) => {
  if (method === "PUT") {
    if (saveFails) return errorResponse(500, 5000001);
    // 서버가 그 값을 그대로 돌려준다 — 실제 서버와 같은 모양이다.
    return jsonResponse<NotificationPreference>(body as NotificationPreference);
  }
  return jsonResponse<NotificationPreference>(preference);
});

beforeEach(() => {
  preference = { enabled: true, sendHour: 9 };
  saveFails = false;
});

const toggle = () => screen.getByLabelText("알림 메일 받기");
const hourSelect = () => screen.queryByLabelText("받을 시각");

describe("불러오기", () => {
  it("저장된 값을 보여 준다", async () => {
    preference = { enabled: true, sendHour: 21 };

    renderWithProviders(<NotificationSection />);

    expect(await screen.findByLabelText("알림 메일 받기")).toBeChecked();
    expect(hourSelect()).toHaveValue("21");
  });

  it("못 읽으면 알린다", async () => {
    failFetch();

    renderWithProviders(<NotificationSection />);

    expect(await screen.findByRole("alert")).toHaveTextContent("불러오지 못했어요");
  });
});

describe("켜고 끄기", () => {
  it("끄면 서버에 저장한다", async () => {
    const { user } = renderWithProviders(<NotificationSection />);
    await screen.findByLabelText("알림 메일 받기");

    await user.click(toggle());

    await vi.waitFor(() =>
      expect(sent.last).toMatchObject({
        url: `${API_URL}/api/v1/me/notifications`,
        method: "PUT",
      }),
    );
    expect(lastBody(sent)).toEqual({ enabled: false, sendHour: 9 });
  });

  /*
   * 끌 때 시각을 날리지 않는다. 다시 켰을 때 9시가 0시로 돌아가 있으면
   * 사용자가 고쳐 둔 것이 조용히 사라진 셈이다.
   */
  it("끄면서 시각은 그대로 보낸다", async () => {
    preference = { enabled: true, sendHour: 21 };
    const { user } = renderWithProviders(<NotificationSection />);
    await screen.findByLabelText("알림 메일 받기");

    await user.click(toggle());

    await vi.waitFor(() => expect(lastBody(sent)).toEqual({ enabled: false, sendHour: 21 }));
  });
});

describe("받을 시각", () => {
  /*
   * 이 화면의 판단이다. 끈 사람에게 시각을 묻지 않는다 — 고를 수 있으면 뭔가 오는 줄 알고,
   * 안 오면 고장이라고 여긴다.
   */
  it("꺼져 있으면 묻지 않는다", async () => {
    preference = { enabled: false, sendHour: 9 };

    renderWithProviders(<NotificationSection />);

    await screen.findByLabelText("알림 메일 받기");
    expect(hourSelect()).not.toBeInTheDocument();
  });

  it("켜면 물어본다", async () => {
    renderWithProviders(<NotificationSection />);

    expect(await screen.findByLabelText("받을 시각")).toBeInTheDocument();
  });

  it("고르면 저장한다", async () => {
    const { user } = renderWithProviders(<NotificationSection />);
    await screen.findByLabelText("받을 시각");

    await user.selectOptions(screen.getByLabelText("받을 시각"), "21");

    await vi.waitFor(() => expect(lastBody(sent)).toEqual({ enabled: true, sendHour: 21 }));
  });

  // 자정은 "0시"보다 "밤 12시"가 읽기 쉽다. 낮 12시와 섞이지 않는지도 함께 본다.
  it("자정과 정오를 사람 말로 적는다", async () => {
    renderWithProviders(<NotificationSection />);
    await screen.findByLabelText("받을 시각");

    expect(screen.getByRole("option", { name: "밤 12시" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "낮 12시" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "오전 9시" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "오후 9시" })).toBeInTheDocument();
  });

  it("스물네 시각을 모두 고를 수 있다", async () => {
    renderWithProviders(<NotificationSection />);
    await screen.findByLabelText("받을 시각");

    expect(screen.getAllByRole("option")).toHaveLength(24);
  });
});

describe("저장 실패", () => {
  /*
   * **읽기는 살려 둔다.** 저장이 실패하면 훅이 `invalidateQueries` 로 다시 읽는데,
   * 그 재조회까지 실패하면 "불러오지 못했어요" 가 화면 전체를 덮어 저장 오류 문구에 닿지 못한다.
   * 서버가 통째로 죽은 경우엔 그게 맞는 그림이지만, 여기서 보려는 것은 저장 실패 쪽이다.
   */
  it("알린다", async () => {
    const { user } = renderWithProviders(<NotificationSection />);
    await screen.findByLabelText("알림 메일 받기");

    saveFails = true;
    await user.click(toggle());

    expect(await screen.findByRole("alert")).toHaveTextContent("바꾸지 못했어요");
  });

  // 실패했는데 스위치가 켜진 채로 남으면 저장된 줄 안다. 서버 값으로 되돌아와야 한다.
  it("되돌린다", async () => {
    const { user } = renderWithProviders(<NotificationSection />);
    await screen.findByLabelText("알림 메일 받기");

    saveFails = true;
    await user.click(toggle());
    await screen.findByRole("alert");

    expect(toggle()).toBeChecked();
  });
});
