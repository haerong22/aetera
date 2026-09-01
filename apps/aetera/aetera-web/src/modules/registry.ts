import type { FrontendModule } from "./types";
import { scheduleModule } from "./schedule";
import { resignationModule } from "./resignation";
import { movingModule } from "./moving";
import { yearEndTaxModule } from "./year-end-tax";
import { renewalModule } from "./renewal";
import { goalModule } from "./goal";
import { expenseModule } from "./expense";
import { assetModule } from "./asset";

/**
 * 배포된 프론트엔드 모듈의 목록. 새 모듈은 여기 한 줄을 추가하면
 * 사이드바·대시보드·모듈 스토어에 자동으로 나타난다.
 *
 * 실제 노출 여부는 항상 서버(`GET /api/v1/me/modules`)의 활성화 상태가 결정한다 —
 * 이 목록은 "코드가 배포된 모듈"의 정적 레지스트리일 뿐이다.
 */
export const frontendModules: FrontendModule[] = [
  scheduleModule,
  resignationModule,
  movingModule,
  yearEndTaxModule,
  renewalModule,
  goalModule,
  expenseModule,
  assetModule,
];

export const moduleById: ReadonlyMap<string, FrontendModule> = new Map(
  frontendModules.map((module) => [module.id, module]),
);

function assertOneProviderPerCapability(modules: FrontendModule[]) {
  const providers = new Map<string, string[]>();
  for (const module of modules) {
    for (const capability of Object.keys(module.capabilities ?? {})) {
      providers.set(capability, [...(providers.get(capability) ?? []), module.id]);
    }
  }
  for (const [capability, ids] of providers) {
    if (ids.length > 1) {
      throw new Error(`능력 '${capability}' 를 여러 모듈이 제공합니다: ${ids.join(", ")}`);
    }
  }
}

assertOneProviderPerCapability(frontendModules);
