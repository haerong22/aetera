import type { FrontendModule } from "./types";
import { scheduleModule } from "./schedule";

/**
 * 배포된 프론트엔드 모듈의 목록. 새 모듈은 여기 한 줄을 추가하면
 * 사이드바·대시보드·모듈 스토어에 자동으로 나타난다.
 *
 * 실제 노출 여부는 항상 서버(`GET /api/v1/me/modules`)의 활성화 상태가 결정한다 —
 * 이 목록은 "코드가 배포된 모듈"의 정적 레지스트리일 뿐이다.
 */
export const frontendModules: FrontendModule[] = [scheduleModule];

export const moduleById: ReadonlyMap<string, FrontendModule> = new Map(
  frontendModules.map((module) => [module.id, module]),
);
