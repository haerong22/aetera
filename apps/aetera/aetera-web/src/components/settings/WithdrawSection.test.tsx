import { beforeEach, describe, expect, it, vi } from "vitest";
import { fireEvent, screen } from "@testing-library/react";
import type { UserEvent } from "@testing-library/user-event";
import { renderWithProviders } from "@/test/render";
import { failFetch, stubFetch } from "@/test/stubFetch";
import { API_URL } from "@/lib/api-client";
import { WithdrawSection } from "./WithdrawSection";

/**
 * 탈퇴는 **되돌릴 수 없다.** 그런데 실수로 누르는 것을 막는 것은 확인 문구 비교 한 줄뿐이고,
 * 그 줄이 헐거워지면 잘못 누른 사람의 모든 기록이 사라진 뒤에야 알게 된다. 서버는 부르면
 * 지울 뿐이라 여기서 못 막으면 아무도 못 막는다.
 *
 * 그래서 이 시험이 보는 것은 "잘 그려지는가"가 아니라 **"쉽게 열리지 않는가"** 다.
 */

const { logout } = vi.hoisted(() => ({ logout: vi.fn() }));

/*
 * 진짜 AuthProvider 를 세우면 세션 복원 요청부터 흉내 내야 하는데, 여기서 볼 것은
 * 지운 뒤에 `logout` 이 불리는가 하나다. 그 하나만 들여다볼 수 있게 갈아 끼운다.
 */
vi.mock("@/lib/auth", () => ({ useAuth: () => ({ logout }) }));

/** 화면이 요구하는 확인 문구. 화면이 export 하지 않으므로 같은 값을 손으로 적는다 — 화면이 바꾸면 여기서 깨져야 한다. */
const CONFIRM_WORD = "탈퇴합니다";

/** 기본은 지워 주는 서버(204). 실패를 보는 시험만 [failFetch] 로 덮어쓴다. */
const sent = stubFetch(() => new Response(null, { status: 204 }));

beforeEach(() => {
  logout.mockClear();
});

const confirmInput = () => screen.getByLabelText("확인");
/** 다이얼로그 안의 마지막 단추. 카드의 "탈퇴하기" 와는 이름이 다르다. */
const confirmButton = () => screen.getByRole("button", { name: "탈퇴" });

async function openDialog(user: UserEvent) {
  await user.click(screen.getByRole("button", { name: "탈퇴하기" }));
  return confirmInput();
}

/** 확인 문구까지 적어 **단추가 열린** 데까지. 여러 시험이 여기서 출발한다. */
async function armed(): Promise<UserEvent> {
  const { user } = renderWithProviders(<WithdrawSection />);
  await user.type(await openDialog(user), CONFIRM_WORD);
  return user;
}

/** 거기서 한 번 더 눌러 **실제로 보내기**까지. */
async function submit(): Promise<UserEvent> {
  const user = await armed();
  await user.click(confirmButton());
  return user;
}

describe("확인 문구", () => {
  it("열기만 해서는 단추가 잠겨 있다", async () => {
    const { user } = renderWithProviders(<WithdrawSection />);

    await openDialog(user);

    expect(confirmButton()).toBeDisabled();
  });

  // 실수로 누르는 것을 막는 것이 이 화면의 전부다. 문구를 다 적어야 비로소 열린다.
  it("다 적으면 열린다", async () => {
    await armed();

    expect(confirmButton()).toBeEnabled();
  });

  it("앞부분만 적으면 잠긴 채다", async () => {
    const { user } = renderWithProviders(<WithdrawSection />);

    await user.type(await openDialog(user), "탈퇴");

    expect(confirmButton()).toBeDisabled();
  });

  it("비슷한 다른 말은 통하지 않는다", async () => {
    const { user } = renderWithProviders(<WithdrawSection />);

    await user.type(await openDialog(user), "탈퇴할게요");

    expect(confirmButton()).toBeDisabled();
  });

  /*
   * 한 번 열렸다고 열린 채로 있으면, 지웠다가 다시 적는 사이에 눌러도 나간다.
   * 문구를 들고 있는 것은 상태 하나이므로 파생을 빠뜨리기 쉬운 자리다.
   */
  it("다 적었다가 지우면 다시 잠긴다", async () => {
    const user = await armed();

    await user.type(confirmInput(), "{Backspace}");

    expect(confirmButton()).toBeDisabled();
  });

  /*
   * 두 번째 층. 단추의 `disabled` 는 화면의 약속일 뿐이라 — 엔터, 자동완성, 나중에 끼워 넣은
   * 다른 단추로 얼마든지 우회된다. 보내는 쪽에서도 한 번 더 막는지 직접 확인한다.
   */
  it("양식을 곧바로 보내도 문구가 틀리면 나가지 않는다", async () => {
    const { user } = renderWithProviders(<WithdrawSection />);
    const input = await openDialog(user);
    await user.type(input, "탈퇴");

    const form = input.closest("form");
    if (!form) throw new Error("확인 칸이 양식 안에 없다 — 시험이 보려던 것을 못 본다");
    fireEvent.submit(form);

    expect(sent.calls).toHaveLength(0);
  });
});

/*
 * 적어 둔 문구가 다시 열 때까지 남아 있으면 확인 문구는 **첫 번째 열기에만** 일한다.
 * 두 번째부터는 열자마자 단추가 열려 있어 한 번 누르면 지워진다 — 마음을 바꿔 취소한
 * 사람이 나중에 잘못 누르는 것이 바로 이 문구가 막으려던 경우다. 닫는 길이 둘이라
 * 둘 다 본다(취소 단추, 그리고 바깥·X·Esc 가 함께 쓰는 `onClose`).
 */
describe("닫았다 다시 열기", () => {
  it("취소한 뒤 다시 열면 처음부터다", async () => {
    const user = await armed();

    await user.click(screen.getByRole("button", { name: "취소" }));
    await openDialog(user);

    expect(confirmInput()).toHaveValue("");
    expect(confirmButton()).toBeDisabled();
  });

  it("바깥을 눌러 닫아도 처음부터다", async () => {
    const user = await armed();

    // 바깥(딤)과 X 는 이름이 같다. 둘 다 같은 `onClose` 로 간다.
    await user.click(screen.getAllByRole("button", { name: "닫기" })[0]);
    await openDialog(user);

    expect(confirmInput()).toHaveValue("");
    expect(confirmButton()).toBeDisabled();
  });

  it("실패 안내도 남지 않는다", async () => {
    failFetch();
    const user = await submit();
    await screen.findByRole("alert");

    await user.click(screen.getByRole("button", { name: "취소" }));
    await openDialog(user);

    // 지난번 실패가 남아 있으면 아직 아무것도 안 했는데 빨간 글씨부터 보인다.
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });
});

describe("지우기", () => {
  it("문구를 적고 누르면 계정을 지운다", async () => {
    await submit();

    expect(sent.last).toMatchObject({ url: `${API_URL}/api/v1/me`, method: "DELETE" });
  });

  it("지운 뒤에는 손님으로 돌아간다", async () => {
    await submit();

    // 계정이 없는데 로그인한 화면에 남아 있으면, 누르는 것마다 알 수 없는 오류가 된다.
    expect(logout).toHaveBeenCalled();
  });

  /*
   * 성공했으면 `busy` 를 풀지 않는다 — 곧 화면 전체가 손님 화면으로 바뀌기 때문이다.
   * 풀어 버리면 그 사이에 한 번 더 눌릴 수 있고, 두 번째는 없는 계정을 지우려다
   * 오류로 끝난다. 성공했는데 실패로 보인다.
   */
  it("성공한 뒤에도 다시 누를 수 없다", async () => {
    await submit();

    expect(screen.getByRole("button", { name: "지우는 중" })).toBeDisabled();
  });

  /*
   * 실패했는데 로그아웃까지 해 버리면 계정은 그대로인 채 로그인 화면으로 튕긴다.
   * 사용자는 탈퇴된 줄 알고 떠나고, 데이터는 남는다.
   */
  it("실패하면 세션을 끝내지 않고 알린다", async () => {
    failFetch();

    await submit();

    expect(await screen.findByRole("alert")).toHaveTextContent("탈퇴하지 못했어요");
    expect(logout).not.toHaveBeenCalled();
  });

  it("실패한 뒤에는 다시 시도할 수 있다", async () => {
    failFetch();

    await submit();
    await screen.findByRole("alert");

    // 문구는 그대로 남아 있으니 단추도 다시 눌릴 수 있어야 한다.
    expect(confirmButton()).toBeEnabled();
  });

  it("취소하면 아무것도 지우지 않는다", async () => {
    const user = await armed();

    await user.click(screen.getByRole("button", { name: "취소" }));

    expect(sent.calls).toHaveLength(0);
    expect(logout).not.toHaveBeenCalled();
  });
});
