import { afterEach } from "vitest";
import { cleanup } from "@testing-library/react";
import "@testing-library/jest-dom/vitest";

/**
 * 시험마다 그린 것을 지운다. 안 지우면 다음 시험이 앞 시험의 DOM 을 같이 보고,
 * `getByRole` 이 "여럿 찾았다"로 터지거나 **더 나쁘게는 엉뚱한 것을 집는다.**
 */
afterEach(cleanup);
