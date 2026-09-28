import { beforeEach, describe, expect, it, vi } from "vitest";
import { screen } from "@testing-library/react";
import type { UserEvent } from "@testing-library/user-event";
import { renderWithProviders } from "@/test/render";
import { jsonResponse } from "@/test/http";
import { failFetch, stubFetch } from "@/test/stubFetch";
import { API_URL } from "@/lib/api-client";
import { ProfileSection } from "./ProfileSection";
import type { User } from "@/lib/types";

/**
 * 이 화면이 지키는 것 둘.
 *
 * 하나는 **확인 칸**이다. 서버는 새 비밀번호를 한 번만 받으므로 오타를 걸러 줄 곳이
 * 여기밖에 없다 — 놓치면 오타난 비밀번호가 그대로 저장되고, 본인도 다음 로그인에서야 안다.
 *
 * 둘은 **오류를 뭉뚱그리지 않는 것**이다. "지금 비밀번호가 틀렸다"와 "새 비밀번호가 규칙에
 * 안 맞는다"는 고쳐야 할 칸이 다른데, 한 문장으로 묶으면 사용자는 둘 다 다시 적는다.
 */

const me: User = {
  id: "11111111-1111-1111-1111-111111111111",
  email: "hong@example.com",
  nickname: "홍길동",
  timezone: "Asia/Seoul",
  registeredAt: "2026-01-01T00:00:00Z",
};

/**
 * 가짜 AuthProvider.
 *
 * `applyProfile` 이 **실제로 user 를 갈아 끼우고 다시 그린다.** 빈 함수로 두었더니
 * `nickname` prop 이 영영 그대로라 저장 뒤 상태가 한 번도 렌더되지 않았고,
 * "저장했어요" 를 그리는 줄에 시험이 닿지도 못했다 — 그 자리에 버그가 있었는데
 * 시험 18개 중 어느 것도 보지 못했다.
 */
const { store, applyProfile } = vi.hoisted(() => {
  const store = { user: null as unknown, listeners: new Set<() => void>() };
  const applyProfile = vi.fn((updated: unknown) => {
    store.user = updated;
    store.listeners.forEach((notify) => notify());
  });
  return { store, applyProfile };
});

vi.mock("@/lib/auth", async () => {
  const { useEffect, useState } = await import("react");
  return {
    useAuth: () => {
      const [, redraw] = useState(0);
      useEffect(() => {
        const notify = () => redraw((tick) => tick + 1);
        store.listeners.add(notify);
        return () => void store.listeners.delete(notify);
      }, []);
      return { user: store.user, applyProfile };
    },
  };
});

/** 기본은 받아 주는 서버. 닉네임은 다듬어서 돌려준다 — 진짜 서버가 그렇게 한다. */
const sent = stubFetch(({ url, body }) => {
  if (url.endsWith("/api/v1/me/password")) return new Response(null, { status: 204 });
  const nickname = String((body as { nickname: string }).nickname).trim();
  return jsonResponse<User>({ ...me, nickname });
});

beforeEach(() => {
  applyProfile.mockClear();
  store.user = me;
});

const nicknameInput = () => screen.getByLabelText("이름");
const saveNicknameButton = () => screen.getByRole("button", { name: "이름 바꾸기" });

/** 비밀번호 다이얼로그를 세 칸 모두 채운 데까지. */
async function fillPasswords(
  user: UserEvent,
  { current = "password1234", next = "newpassword5678", again = "newpassword5678" } = {},
) {
  await user.click(screen.getByRole("button", { name: "비밀번호 바꾸기" }));
  if (current) await user.type(screen.getByLabelText("지금 비밀번호"), current);
  if (next) await user.type(screen.getByLabelText("새 비밀번호"), next);
  if (again) await user.type(screen.getByLabelText("새 비밀번호 확인"), again);
}

const submitButton = () => screen.getByRole("button", { name: "바꾸기" });

describe("이메일", () => {
  /*
   * 바꿀 수 있는 척하지 않는다. 이메일은 로그인 아이디이자 알림이 가는 곳이라,
   * 새 주소가 진짜 그 사람 것인지 확인하는 절차가 먼저 있어야 한다.
   */
  it("보여 주기만 하고 고칠 수 없다", () => {
    renderWithProviders(<ProfileSection />);

    expect(screen.getByText("hong@example.com")).toBeInTheDocument();
    expect(screen.queryByLabelText("이메일")).not.toBeInTheDocument();
  });
});

describe("이름 바꾸기", () => {
  it("지금 이름이 칸에 들어 있다", () => {
    renderWithProviders(<ProfileSection />);

    expect(nicknameInput()).toHaveValue("홍길동");
  });

  // 같은 값을 보내면 서버는 받아 주지만 바뀐 것이 없다. "저장됨"만 깜빡이고 끝난다.
  it("안 바꿨으면 단추가 잠겨 있다", () => {
    renderWithProviders(<ProfileSection />);

    expect(saveNicknameButton()).toBeDisabled();
  });

  it("공백만 다른 것도 잠긴 채다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);

    await user.type(nicknameInput(), "  ");

    expect(saveNicknameButton()).toBeDisabled();
  });

  it("비우면 잠긴다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);

    await user.clear(nicknameInput());

    expect(saveNicknameButton()).toBeDisabled();
  });

  it("바꾸면 서버로 보낸다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);

    await user.clear(nicknameInput());
    await user.type(nicknameInput(), "임꺽정");
    await user.click(saveNicknameButton());

    await vi.waitFor(() =>
      expect(sent.last).toMatchObject({ url: `${API_URL}/api/v1/me`, method: "PUT" }),
    );
    expect(sent.last?.body).toEqual({ nickname: "임꺽정" });
  });

  /*
   * 사이드바와 인사말은 auth 컨텍스트의 `user` 를 보고 그린다. 반영하지 않으면
   * 저장은 됐는데 화면 곳곳에 옛 이름이 남아 안 바뀐 것처럼 보인다.
   */
  it("바꾼 이름을 앱 전체에 반영한다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);

    await user.clear(nicknameInput());
    await user.type(nicknameInput(), "임꺽정");
    await user.click(saveNicknameButton());

    // 첫 인자만 본다 — TanStack 은 `onSuccess` 에 변수·컨텍스트까지 함께 넘긴다.
    await vi.waitFor(() => expect(applyProfile).toHaveBeenCalled());
    expect(applyProfile.mock.calls[0][0]).toMatchObject({ nickname: "임꺽정" });
  });

  it("실패하면 서버가 준 이유를 보여 준다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);
    await user.clear(nicknameInput());
    await user.type(nicknameInput(), "임꺽정");

    failFetch(400, 4000102, "닉네임은 1~30자여야 합니다.");
    await user.click(saveNicknameButton());

    expect(await screen.findByRole("alert")).toHaveTextContent("닉네임은 1~30자여야 합니다.");
    expect(applyProfile).not.toHaveBeenCalled();
  });

  /** 이름을 바꿔 저장한 뒤까지. 여기서부터가 "지난 결과가 남는가" 를 보는 자리다. */
  async function renameTo(user: UserEvent, name: string) {
    await user.clear(nicknameInput());
    await user.type(nicknameInput(), name);
    await user.click(saveNicknameButton());
  }

  it("저장하면 저장했다고 말한다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);

    await renameTo(user, "임꺽정");

    expect(await screen.findByRole("status")).toHaveTextContent("저장했어요");
  });

  /*
   * `isSuccess` 는 한 번 켜지면 다음 요청까지 안 꺼진다. 그대로 두면 고쳐 적는 동안
   * "저장했어요" 가 계속 붙어 있어, 아직 안 보낸 값을 보낸 것으로 읽게 된다.
   */
  it("고쳐 적기 시작하면 지난 결과를 치운다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);
    await renameTo(user, "임꺽정");
    await screen.findByRole("status");

    await user.type(nicknameInput(), "X");

    expect(screen.queryByRole("status")).not.toBeInTheDocument();
  });

  // 되돌려 적으면 처음 상태와 같다 — 저장한 적 없는 값에 "저장했어요" 가 붙으면 안 된다.
  it("고쳤다 되돌려도 다시 뜨지 않는다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);
    await renameTo(user, "임꺽정");
    await screen.findByRole("status");

    await user.type(nicknameInput(), "X");
    await user.type(nicknameInput(), "{Backspace}");

    expect(screen.queryByRole("status")).not.toBeInTheDocument();
  });

  it("고쳐 적기 시작하면 실패 문구도 치운다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);
    await user.clear(nicknameInput());
    await user.type(nicknameInput(), "임꺽정");
    failFetch(400, 4000102, "닉네임은 1~30자여야 합니다.");
    await user.click(saveNicknameButton());
    await screen.findByRole("alert");

    await user.type(nicknameInput(), "X");

    // 이미 고쳐 적은 값 위에 옛 이유가 남아 있으면, 고쳤는데도 여전히 틀린 줄 안다.
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });
});

describe("비밀번호 바꾸기", () => {
  it("세 칸을 다 채워야 열린다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);

    await fillPasswords(user, { again: "" });

    expect(submitButton()).toBeDisabled();
  });

  /*
   * 서버는 확인 칸을 모른다 — 오타를 걸러 주는 것은 여기밖에 없다.
   * 놓치면 오타난 비밀번호가 저장되고, 본인도 다음 로그인에서야 알게 된다.
   */
  it("새 비밀번호 두 칸이 다르면 막고 알린다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);

    await fillPasswords(user, { again: "newpassword9999" });

    expect(screen.getByRole("alert")).toHaveTextContent("서로 달라요");
    expect(submitButton()).toBeDisabled();
  });

  it("맞춰 적으면 서버로 보낸다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);

    await fillPasswords(user);
    await user.click(submitButton());

    await vi.waitFor(() =>
      expect(sent.last).toMatchObject({ url: `${API_URL}/api/v1/me/password`, method: "PUT" }),
    );
    expect(sent.last?.body).toEqual({
      currentPassword: "password1234",
      newPassword: "newpassword5678",
    });
  });

  /*
   * 다른 기기가 끊긴 것은 화면에 보이지 않는 결과다. 말해 주지 않으면 다음에 그 기기에서
   * 로그아웃돼 있는 것을 보고 고장이라고 여긴다.
   */
  it("성공하면 다른 기기가 로그아웃됐다고 알린다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);

    await fillPasswords(user);
    await user.click(submitButton());

    expect(await screen.findByRole("status")).toHaveTextContent("다른 기기는 로그아웃됐어요");
  });

  it("성공하면 다이얼로그를 닫는다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);

    await fillPasswords(user);
    await user.click(submitButton());

    await vi.waitFor(() => expect(screen.queryByLabelText("지금 비밀번호")).not.toBeInTheDocument());
  });

  it("지금 비밀번호가 틀리면 그렇다고 말한다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);
    await fillPasswords(user);

    failFetch(400, 4000203, "현재 비밀번호가 올바르지 않습니다.");
    await user.click(submitButton());

    expect(await screen.findByRole("alert")).toHaveTextContent("현재 비밀번호가 올바르지 않습니다.");
  });

  // 위 시험과 다른 문구가 나와야 한다. 같으면 사용자는 어느 칸을 고칠지 모른다.
  it("새 비밀번호가 규칙에 안 맞으면 다른 말을 한다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);
    await fillPasswords(user);

    failFetch(400, 4000201, "비밀번호는 8자 이상 64자 이하여야 합니다.");
    await user.click(submitButton());

    expect(await screen.findByRole("alert")).toHaveTextContent("8자 이상");
  });

  it("실패하면 다이얼로그를 닫지 않는다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);
    await fillPasswords(user);

    failFetch(400, 4000203, "현재 비밀번호가 올바르지 않습니다.");
    await user.click(submitButton());
    await screen.findByRole("alert");

    // 닫아 버리면 적어 둔 것이 사라져 처음부터 다시 적어야 한다.
    expect(screen.getByLabelText("지금 비밀번호")).toBeInTheDocument();
  });

  it("취소하면 아무것도 보내지 않는다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);

    await fillPasswords(user);
    await user.click(screen.getByRole("button", { name: "취소" }));

    expect(sent.calls).toHaveLength(0);
  });

  it("취소한 뒤 다시 열면 칸이 비어 있다", async () => {
    const { user } = renderWithProviders(<ProfileSection />);
    await fillPasswords(user);

    await user.click(screen.getByRole("button", { name: "취소" }));
    await user.click(screen.getByRole("button", { name: "비밀번호 바꾸기" }));

    expect(screen.getByLabelText("지금 비밀번호")).toHaveValue("");
    expect(screen.getByLabelText("새 비밀번호")).toHaveValue("");
  });
});
