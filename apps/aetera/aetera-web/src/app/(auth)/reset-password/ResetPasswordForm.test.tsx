import { beforeEach, describe, expect, it, vi } from "vitest";
import { screen } from "@testing-library/react";
import type { UserEvent } from "@testing-library/user-event";
import { renderWithProviders } from "@/test/render";
import { failFetch, stubFetch } from "@/test/stubFetch";
import { API_URL } from "@/lib/api-client";
import { ResetPasswordForm } from "./ResetPasswordForm";

/**
 * 링크를 열고 새 비밀번호를 정하는 화면.
 *
 * **확인 칸이 이 화면의 값어치다.** 서버는 새 비밀번호를 한 번만 받으므로 오타를 걸러 줄
 * 곳이 여기뿐인데, 여기서 놓치면 오타난 비밀번호가 저장되고 **그 링크는 이미 죽어서**
 * 다시 요청하는 수밖에 없다. 비밀번호 변경 화면보다 대가가 크다.
 */

const { replace, searchParams } = vi.hoisted(() => ({
  replace: vi.fn(),
  searchParams: { value: new URLSearchParams("token=raw-token") },
}));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace }),
  useSearchParams: () => searchParams.value,
}));

const sent = stubFetch(() => new Response(null, { status: 204 }));

beforeEach(() => {
  replace.mockClear();
  searchParams.value = new URLSearchParams("token=raw-token");
});

const submitButton = () => screen.getByRole("button", { name: "비밀번호 바꾸기" });

async function fill(
  { next = "newpassword5678", again = "newpassword5678" } = {},
): Promise<UserEvent> {
  const { user } = renderWithProviders(<ResetPasswordForm />);
  if (next) await user.type(screen.getByLabelText("새 비밀번호"), next);
  if (again) await user.type(screen.getByLabelText("새 비밀번호 확인"), again);
  return user;
}

describe("링크 없이 열었을 때", () => {
  /*
   * 메일 앱이 주소를 자르거나, 사용자가 일부만 복사해 오는 일이 있다. 빈 칸을 보여 주고
   * 다 적은 뒤에 실패시키면 헛수고를 시키는 셈이라, 들어오자마자 말해 준다.
   */
  it("적기 전에 말해 주고 다시 받을 길을 준다", () => {
    searchParams.value = new URLSearchParams("");

    renderWithProviders(<ResetPasswordForm />);

    expect(screen.getByRole("alert")).toBeInTheDocument();
    expect(screen.queryByLabelText("새 비밀번호")).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: "링크 다시 받기" })).toBeInTheDocument();
  });
});

describe("새 비밀번호", () => {
  it("두 칸이 다르면 막고 알린다", async () => {
    await fill({ again: "newpassword9999" });

    expect(screen.getByRole("alert")).toHaveTextContent("서로 달라요");
    expect(submitButton()).toBeDisabled();
  });

  it("한 칸만 채우면 잠긴 채다", async () => {
    await fill({ again: "" });

    expect(submitButton()).toBeDisabled();
  });

  it("맞춰 적으면 링크의 토큰과 함께 보낸다", async () => {
    const user = await fill();
    await user.click(submitButton());

    await vi.waitFor(() =>
      expect(sent.last).toMatchObject({
        url: `${API_URL}/api/v1/auth/password-reset/confirm`,
        method: "POST",
      }),
    );
    expect(sent.last?.body).toEqual({ token: "raw-token", newPassword: "newpassword5678" });
  });
});

describe("끝난 뒤", () => {
  /*
   * 로그인시키지 않는다. 여기까지 온 사람이 주인이라는 근거는 메일함을 열었다는 것뿐이고,
   * 서버도 세션을 주지 않는다. 화면이 멋대로 로그인한 척하면 서버와 어긋난다.
   */
  it("자동으로 로그인하지 않는다", async () => {
    const user = await fill();
    await user.click(submitButton());

    await screen.findByText(/비밀번호를 바꿨어요/);
    expect(replace).not.toHaveBeenCalled();
  });

  it("다른 기기가 로그아웃됐다고 알린다", async () => {
    const user = await fill();
    await user.click(submitButton());

    expect(await screen.findByText(/모두 로그아웃됐어요/)).toBeInTheDocument();
  });

  it("로그인 화면으로 갈 길을 준다", async () => {
    const user = await fill();
    await user.click(submitButton());

    await user.click(await screen.findByRole("button", { name: "로그인하러 가기" }));

    expect(replace).toHaveBeenCalledWith("/login");
  });
});

describe("죽은 링크", () => {
  it("서버가 준 이유를 그대로 보여 준다", async () => {
    failFetch(401, 4010204, "링크가 만료되었거나 이미 사용되었습니다. 다시 요청해 주세요.");
    const user = await fill();

    await user.click(submitButton());

    expect(await screen.findByRole("alert")).toHaveTextContent("만료되었거나 이미 사용");
  });

  // 실패한 자리에서 다시 받을 길이 보여야 한다 — 없으면 주소창을 직접 고쳐야 한다.
  it("다시 받을 길이 늘 보인다", async () => {
    await fill();

    expect(screen.getByRole("link", { name: "다시 받기" })).toBeInTheDocument();
  });

  it("실패하면 성공 화면으로 넘어가지 않는다", async () => {
    failFetch(401, 4010204, "링크가 만료되었습니다.");
    const user = await fill();

    await user.click(submitButton());
    await screen.findByRole("alert");

    expect(screen.queryByText(/비밀번호를 바꿨어요/)).not.toBeInTheDocument();
  });
});
