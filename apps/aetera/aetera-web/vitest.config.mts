import { defineConfig } from "vitest/config";
import { fileURLToPath } from "node:url";

/**
 * 계산만 시험한다. jsdom 도, 렌더러도 넣지 않았다.
 *
 * 이 저장소에서 손으로 잡은 버그는 전부 순수 함수 안에 있었다 — 바닥나는 달이 한 달 당겨지고,
 * 두 합계의 차가 0이 되고, 조사가 틀리는 식이다. 화면을 그려 보는 시험은 붙이는 값이 다르므로,
 * 필요해질 때 그때 넣는다. **지금 없는 것은 러너가 아니라 숫자에 대한 확인이다.**
 */
export default defineConfig({
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  test: {
    /*
     * `.ts` 만 본다 — `.tsx` 를 넣어도 jsdom 이 없어 돌지 않는다.
     * 컴포넌트 시험을 들이려면 환경부터 붙여야 하고, 그때 이 줄도 함께 넓힌다.
     * 그 전까지 `.test.tsx` 를 만들면 **조용히 안 돌므로** 여기 적어 둔다.
     */
    include: ["src/**/*.test.ts"],
  },
});
