import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import { fileURLToPath } from "node:url";

const alias = { "@": fileURLToPath(new URL("./src", import.meta.url)) };

/**
 * 시험을 둘로 나눠 돌린다.
 *
 * `.test.ts` 는 계산만 보므로 node 에서 그대로 돈다. `.test.tsx` 는 화면을 그려 보느라
 * jsdom 이 필요한데, 그건 켜는 값이 싸지 않다 — 확장자로 갈라 두면 대부분을 차지하는
 * 계산 시험이 브라우저 흉내를 지고 가지 않는다.
 *
 * JSX 변환은 `dom` 쪽에만 붙인다. 앱 빌드에서는 Next 가 하던 일인데 여기서는
 * 아무도 해 주지 않아, 플러그인 없이는 시험 파일의 태그에서 파싱이 멈춘다.
 */
export default defineConfig({
  resolve: { alias },
  test: {
    projects: [
      {
        resolve: { alias },
        test: {
          name: "unit",
          include: ["src/**/*.test.ts"],
        },
      },
      {
        plugins: [react()],
        resolve: { alias },
        test: {
          name: "dom",
          include: ["src/**/*.test.tsx"],
          environment: "jsdom",
          setupFiles: ["./src/test/setup.ts"],
        },
      },
    ],
  },
});
