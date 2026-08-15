import { DoorOpen } from "lucide-react";
import type { FrontendModule } from "../types";
import { GuidePage } from "../guide/GuidePage";
import { RESIGNATION_MODULE_ID } from "./id";

const TITLE = "퇴사 준비";

function ResignationPage() {
  return <GuidePage moduleTitle={TITLE} guideId={RESIGNATION_MODULE_ID} />;
}

/**
 * 백엔드 `ResignationModule` 의 프론트엔드 짝.
 *
 * 화면도 콘텐츠도 공용이라 이 파일이 이 모듈의 전부다 — 다음 가이드(결혼 준비, 이사)도
 * 백엔드에 템플릿 하나, 프론트엔드에 이런 파일 하나로 붙는다.
 */
export const resignationModule: FrontendModule = {
  id: RESIGNATION_MODULE_ID,
  title: TITLE,
  icon: DoorOpen,
  Page: ResignationPage,
  queryKeyPrefix: RESIGNATION_MODULE_ID,
};
