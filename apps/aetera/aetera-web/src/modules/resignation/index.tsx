import { DoorOpen } from "lucide-react";
import { guideModule } from "../guide/module";
import { RunwayTool } from "./RunwayTool";
import { SeveranceTaxTool } from "./SeveranceTaxTool";

export const resignationModule = guideModule({
  id: "resignation",
  title: "퇴사 준비",
  icon: DoorOpen,
  // 공용 가이드 화면에 넣으면 이사 준비 가이드까지 이 도구들을 짊어지므로, 붙일 자리를 여기서 정한다.
  taskTools: {
    // "생활비가 몇 달 버티는지 계산하기" 항목 아래에 버틸 개월 수 계산기를 접어서 붙인다.
    "finance-runway": RunwayTool,
    // "IRP 계좌 확인하기" 항목 아래에 퇴직급여 세금 계산기를 접어서 붙인다.
    "irp-account": SeveranceTaxTool,
  },
});
