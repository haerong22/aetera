import { Puzzle, type LucideIcon } from "lucide-react";
import type { FrontendModule } from "./types";
import { scheduleModule } from "./schedule";
import { resignationModule } from "./resignation";
import { movingModule } from "./moving";
import { jobChangeModule } from "./job-change";
import { yearEndTaxModule } from "./year-end-tax";
import { renewalModule } from "./renewal";
import { goalModule } from "./goal";
import { expenseModule } from "./expense";
import { assetModule } from "./asset";
import { incomeModule } from "./income";
import { timelineModule } from "./timeline";

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
  jobChangeModule,
  yearEndTaxModule,
  renewalModule,
  goalModule,
  expenseModule,
  assetModule,
  incomeModule,
  timelineModule,
];

export const moduleById: ReadonlyMap<string, FrontendModule> = new Map(
  frontendModules.map((module) => [module.id, module]),
);

/**
 * 모듈의 아이콘. 모르는 아이디면 조각 모양으로 대신한다.
 *
 * 서버가 프론트엔드보다 앞서 배포되면 **화면 코드가 없는 모듈이 목록에 섞여 온다.**
 * 그때 폴백이 한 군데 있어야 세 화면이 같은 모양을 보여준다.
 */
export function moduleIcon(moduleId: string): LucideIcon {
  return moduleById.get(moduleId)?.icon ?? Puzzle;
}

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
