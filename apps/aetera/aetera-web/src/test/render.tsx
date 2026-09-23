import type { ReactElement, ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

/**
 * 시험용 쿼리 클라이언트.
 *
 * 재시도를 끈다 — 실패를 보려는 시험이 기본 세 번의 재시도를 기다리느라 타임아웃으로
 * 끝나고, 그러면 "실패를 알리는가"가 아니라 "느린가"를 재게 된다.
 * **실패를 보는 시험들이 이 설정에 기대고 있다.** 켜면 그쪽이 느려지다 터진다.
 *
 * 시험마다 새로 만든다. 하나를 나눠 쓰면 앞 시험이 채운 캐시를 다음 시험이 보고,
 * 조회가 아예 안 나가서 로딩 상태를 그려 볼 수 없다.
 */
function testQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false, gcTime: 0 },
      mutations: { retry: false },
    },
  });
}

/**
 * 프로바이더로 감싸 그린다. 반환값에 [user] 가 함께 온다 —
 * `userEvent.setup()` 을 시험마다 손으로 부르면 빠뜨린 곳이 생기고,
 * 빠뜨리면 클릭이 먹지 않는 이유를 한참 찾게 된다.
 */
export function renderWithProviders(ui: ReactElement) {
  const client = testQueryClient();
  const wrapper = ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );

  return { user: userEvent.setup(), ...render(ui, { wrapper }) };
}
