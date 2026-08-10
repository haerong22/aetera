"use client";

import { use } from "react";
import { notFound } from "next/navigation";
import { moduleById } from "@/modules/registry";

/**
 * 모든 모듈 화면의 공통 라우트. 모듈 코드는 전부 src/modules/{id} 에 살고,
 * 여기서는 레지스트리를 찾아 그 모듈의 Page 를 렌더하기만 한다 —
 * 모듈이 늘어도 라우트 파일을 새로 만들지 않는다.
 *
 * 배포되지 않은 모듈 경로는 404. 켜지 않은 모듈은 서버 가드가 403 을 주고,
 * 모듈 화면이 그걸 안내로 바꾼다.
 */
export default function ModuleRoute({ params }: { params: Promise<{ moduleId: string }> }) {
  const { moduleId } = use(params);
  const module = moduleById.get(moduleId);
  if (!module) notFound();

  const Page = module.Page;
  return <Page />;
}
