import type { ComponentType } from "react";
import type { LucideIcon } from "lucide-react";

/**
 * 프론트엔드 모듈 정의 — 백엔드 `AeteraModule` SPI 의 거울.
 * `id` 는 백엔드 `ModuleId` 와 반드시 같아야 한다 (모듈 스토어·가드·라우트가 같은 값을 쓴다).
 *
 * 사이드바 메뉴, 라우트(`/m/{id}`), 캐시 정리가 모두 이 정의에서 나온다.
 * 그래서 새 모듈을 붙일 때 코어 화면 코드는 건드리지 않는다.
 */
export interface FrontendModule {
  id: string;
  /** 사이드바·모듈 스토어에 보이는 이름. */
  title: string;
  icon: LucideIcon;
  /** 모듈 화면. `/m/{id}` 라우트가 이걸 렌더한다. */
  Page: ComponentType;
  /**
   * 이 모듈이 쓰는 TanStack Query 키의 접두사.
   * 모듈을 껐다 켤 때 코어가 이 접두사로 캐시를 비운다 — 규칙으로만 두면
   * 다른 접두사를 쓴 모듈이 조용히 낡은 데이터를 보여준다.
   */
  queryKeyPrefix: string;
}

/** 모듈 화면 경로. 라우트와 링크가 같은 규칙을 쓰도록 한곳에서 만든다. */
export function modulePath(moduleId: string): string {
  return `/m/${moduleId}`;
}
