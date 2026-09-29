import { describe, expect, it, vi } from "vitest";
import { screen } from "@testing-library/react";
import { renderWithProviders } from "@/test/render";
import { failFetch, stubFetch } from "@/test/stubFetch";
import { API_URL } from "@/lib/api-client";
import ForgotPasswordPage from "./page";

/**
 * 이 화면이 지키는 것은 **가입 여부를 말하지 않는 것**이다.
 *
 * 서버는 있는 주소와 없는 주소에 똑같이 204 로 답한다. 그런데 화면이 "보냈어요"라고
 * 단정하면 그 구분을 **화면이 대신 해 버린다** — 남의 주소를 넣어 본 사람이
 * "보냈다니 가입했구나"로 읽는다. 서버에서 막은 것을 화면에서 여는 셈이다.
 */

const sent = stubFetch(() => new Response(null, { status: 204 }));

const emailInput = () => screen.getByLabelText("이메일");
const submitButton = () => screen.getByRole("button", { name: "링크 받기" });

async function requestFor(email = "hong@example.com") {
  const { user } = renderWithProviders(<ForgotPasswordPage />);
  await user.type(emailInput(), email);
  await user.click(submitButton());
  return user;
}

describe("링크 요청", () => {
  it("적은 주소로 요청을 보낸다", async () => {
    await requestFor();

    await vi.waitFor(() =>
      expect(sent.last).toMatchObject({
        url: `${API_URL}/api/v1/auth/password-reset`,
        method: "POST",
      }),
    );
    expect(sent.last?.body).toEqual({ email: "hong@example.com" });
  });

  /*
   * "보냈어요" 가 아니라 "가입돼 있다면 보냈어요" 여야 한다. 정말 가입한 사람에게는
   * 같은 뜻이고, 떠보는 사람에게는 아무것도 알려 주지 않는다.
   */
  it("보냈다고 단정하지 않는다", async () => {
    await requestFor();

    const notice = await screen.findByText(/가입돼 있다면/);
    expect(notice).toBeInTheDocument();
  });

  it("어느 주소로 보냈는지 보여 준다", async () => {
    await requestFor("hong@example.com");

    // 오타를 적었을 때 알아차릴 수 있어야 한다 — 안 오는 이유가 그것일 때가 많다.
    expect(await screen.findByText("hong@example.com")).toBeInTheDocument();
  });

  it("얼마나 열려 있는지 알려 준다", async () => {
    await requestFor();

    expect(await screen.findByText(/30분/)).toBeInTheDocument();
  });

  it("다른 주소로 다시 보낼 수 있다", async () => {
    const user = await requestFor();
    await screen.findByText(/가입돼 있다면/);

    await user.click(screen.getByRole("button", { name: "다른 주소로 다시 보내기" }));

    expect(emailInput()).toBeInTheDocument();
  });

  it("실패하면 알린다", async () => {
    failFetch(500, 5000001);
    await requestFor();

    expect(await screen.findByRole("alert")).toBeInTheDocument();
    // 실패했는데 "보냈다" 화면으로 넘어가면 오지 않는 메일을 기다리게 된다.
    expect(screen.queryByText(/가입돼 있다면/)).not.toBeInTheDocument();
  });
});
